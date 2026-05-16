# build-and-run.ps1 - Compile, package, deploy and start Tomcat
# Usage: cd src; .\build-and-run.ps1

# ============================================================
#  CONFIGURE THESE TWO PATHS FOR YOUR MACHINE
# ============================================================
$DefaultJdkHome = 'E:\openjdk-21.0.2_windows-x64_bin\jdk-21.0.2'
$DefaultTomcat   = 'D:\tomcat\apache-tomcat-9.0.116'

$JDK_HOME = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { $DefaultJdkHome }
$TOMCAT   = if ($env:CATALINA_HOME) { $env:CATALINA_HOME } else { $DefaultTomcat }

# 独立 CATALINA_BASE，避免与本机其它 Tomcat/IDEA 抢 8080 / 8005
$HTTP_PORT      = 18080
$SHUTDOWN_PORT  = 18005
$AJP_PORT       = 18009
# ============================================================

function Get-Executable($name, $defaultPath) {
    if (Test-Path $defaultPath) { return $defaultPath }
    $cmd = Get-Command $name -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    return $null
}

$JAVAC       = Get-Executable 'javac.exe' "$JDK_HOME\bin\javac.exe"
$JAR         = Get-Executable 'jar.exe' "$JDK_HOME\bin\jar.exe"
$SERVLET_JAR = "$TOMCAT\lib\servlet-api.jar"

if ($JAVAC) { $env:JAVA_HOME = Split-Path -Parent (Split-Path -Parent $JAVAC) }
if ($TOMCAT) { $env:CATALINA_HOME = $TOMCAT }

Write-Host "Resolved JAVA_HOME=$env:JAVA_HOME"
Write-Host "Resolved CATALINA_HOME=$env:CATALINA_HOME"

# --- Validate paths ---
foreach ($p in @($JAVAC, $JAR, $SERVLET_JAR, "$TOMCAT\bin\startup.bat")) {
    if (-not (Test-Path $p)) { Write-Host "[ERROR] Not found: $p"; exit 1 }
}

$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
$POSSIBLE_BASES = @(
    $SCRIPT_DIR,
    (Split-Path $SCRIPT_DIR -Parent)
)
$BASE = $POSSIBLE_BASES | Where-Object {
    (Test-Path (Join-Path $_ 'src\main\java')) -and (Test-Path (Join-Path $_ 'src\main\webapp'))
} | Select-Object -First 1

if (-not $BASE) {
    Write-Host "[WARN] Could not locate expected repository root. Using script directory as base: $SCRIPT_DIR"
    $BASE = $SCRIPT_DIR
}

function Initialize-TomcatBase {
    param(
        [string]$TomcatHome,
        [string]$TomcatBase,
        [int]$HttpPort,
        [int]$ShutdownPort,
        [int]$AjpPort
    )
    foreach ($d in @('conf', 'logs', 'temp', 'work', 'webapps')) {
        New-Item -ItemType Directory -Force -Path (Join-Path $TomcatBase $d) | Out-Null
    }
    $serverXml = Join-Path $TomcatBase 'conf\server.xml'
    if (-not (Test-Path $serverXml)) {
        Copy-Item (Join-Path $TomcatHome 'conf\*') (Join-Path $TomcatBase 'conf') -Recurse -Force
    }
    $raw = Get-Content $serverXml -Raw -Encoding UTF8
    $httpMarker = ('port="{0}"' -f $HttpPort)
    if ($raw -notmatch [regex]::Escape($httpMarker)) {
        $raw = $raw -replace '<Server port="8005" ', "<Server port=`"$ShutdownPort`" "
        $raw = $raw -replace '<Connector port="8080" ', "<Connector port=`"$HttpPort`" "
        $raw = $raw -replace 'protocol="HTTP/1.1"(\s+)port="8080"', "protocol=`"HTTP/1.1`"`$1port=`"$HttpPort`""
        $raw = $raw -replace 'port="8009"', "port=`"$AjpPort`""
        Set-Content -Path $serverXml -Value $raw -Encoding UTF8 -NoNewline
        Write-Host "Patched server.xml: HTTP=$HttpPort shutdown=$ShutdownPort AJP=$AjpPort"
    }
}

$SRC    = Join-Path $BASE 'src\main\java'
$WEB    = Join-Path $BASE 'src\main\webapp'
$OUT    = Join-Path $BASE 'out'
$OUTBIN = Join-Path $OUT 'WEB-INF\classes'
$WAR    = Join-Path $BASE 'ta-recruitment.war'

# --- 1. Clean ---
Write-Host '[1/4] Cleaning...'
if (Test-Path $OUT) { Remove-Item -Recurse -Force $OUT }
New-Item -ItemType Directory -Force -Path $OUTBIN | Out-Null

# --- 2. Compile ---
Write-Host '[2/4] Compiling...'
$sources = Get-ChildItem -Path $SRC -Recurse -Filter *.java | Sort-Object FullName | Select-Object -ExpandProperty FullName
if (-not $sources) {
    Write-Host "[ERROR] No Java source files found in $SRC"
    exit 1
}
& $JAVAC -source 11 -target 11 -encoding UTF-8 -cp $SERVLET_JAR -d $OUTBIN @sources
if ($LASTEXITCODE -ne 0) { Write-Host '[ERROR] Compilation failed.'; exit 1 }
Write-Host "Compiled $($sources.Count) files OK"

# --- 3. Package ---
Write-Host '[3/4] Packaging WAR...'
Copy-Item -Recurse -Force "$WEB\*" $OUT
$dataDir = "$OUT\WEB-INF\data"
if (-not (Test-Path $dataDir)) { New-Item -ItemType Directory -Force -Path $dataDir | Out-Null }
if (-not (Test-Path "$dataDir\users.json"))    { Set-Content "$dataDir\users.json"    '[]' }
if (-not (Test-Path "$dataDir\profiles.json")) { Set-Content "$dataDir\profiles.json" '[]' }
if (-not (Test-Path "$dataDir\applications.json")) { Set-Content "$dataDir\applications.json" '[]' }
if (-not (Test-Path "$dataDir\favorites.json")) { Set-Content "$dataDir\favorites.json" '[]' }
Push-Location $OUT
& $JAR -cvf $WAR . | Out-Null
Pop-Location
Write-Host "WAR built: $WAR"

# --- 4. Start Tomcat ---
Write-Host '[4/4] Starting Tomcat...'
$TOMCAT_BASE = Join-Path $BASE 'tomcat-base'
Initialize-TomcatBase -TomcatHome $TOMCAT -TomcatBase $TOMCAT_BASE -HttpPort $HTTP_PORT -ShutdownPort $SHUTDOWN_PORT -AjpPort $AJP_PORT
Copy-Item -Force $WAR (Join-Path $TOMCAT_BASE 'webapps\ta-recruitment.war')

$env:CATALINA_BASE = $TOMCAT_BASE
& "$TOMCAT\bin\startup.bat"
Write-Host 'Waiting for Tomcat to start (6s)...'
Start-Sleep -Seconds 6
$loginUrl = "http://localhost:$HTTP_PORT/ta-recruitment/login"
Start-Process $loginUrl
Write-Host "Done!  $loginUrl  (CATALINA_BASE=$TOMCAT_BASE)"