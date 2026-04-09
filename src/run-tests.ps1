# No-Maven test runner:
# 1) compile main sources (reuse existing manual build script)
# 2) compile a lightweight Java smoke-check class
# 3) run the smoke checks with java -ea
#
# Usage:
#   cd src
#   .\run-tests.ps1

$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

$DefaultJdkHome = 'D:\Apps\OpenJDKs\OpenJDK11.0.29'
$DefaultTomcat  = 'D:\Apps\IntelliJ Idea\apache-tomcat-9.0.115'

$JDK_HOME = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { $DefaultJdkHome }
$TOMCAT   = if ($env:CATALINA_HOME) { $env:CATALINA_HOME } else { $DefaultTomcat }

function Get-Executable($name, $defaultPath) {
    if (Test-Path $defaultPath) { return $defaultPath }
    $cmd = Get-Command $name -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    return $null
}

$JAVAC = Get-Executable 'javac.exe' "$JDK_HOME\bin\javac.exe"
$JAVA  = Get-Executable 'java.exe'  "$JDK_HOME\bin\java.exe"
$SERVLET_JAR = "$TOMCAT\lib\servlet-api.jar"

if (-not $JAVAC -or -not $JAVA) {
    Write-Host "[ERROR] javac/java not found. Configure JAVA_HOME or install JDK 11+."
    exit 1
}

if (-not (Test-Path $SERVLET_JAR)) {
    Write-Host "[ERROR] servlet-api.jar not found at: $SERVLET_JAR"
    Write-Host "        Configure CATALINA_HOME or edit DefaultTomcat in run-tests.ps1"
    exit 1
}

Write-Host '[1/3] Compile application classes...'
$SRC_MAIN    = Join-Path $PSScriptRoot 'src\main\java'
$OUT_CLASSES = Join-Path $PSScriptRoot 'out\WEB-INF\classes'
if (Test-Path (Join-Path $PSScriptRoot 'out')) { Remove-Item -Recurse -Force (Join-Path $PSScriptRoot 'out') }
New-Item -ItemType Directory -Force -Path $OUT_CLASSES | Out-Null

$mainSources = Get-ChildItem -Path $SRC_MAIN -Recurse -Filter *.java | Sort-Object FullName | Select-Object -ExpandProperty FullName
if (-not $mainSources) {
    Write-Host "[ERROR] No Java source files found in $SRC_MAIN"
    exit 1
}
& $JAVAC -source 11 -target 11 -encoding UTF-8 -cp $SERVLET_JAR -d $OUT_CLASSES @mainSources
if ($LASTEXITCODE -ne 0) {
    Write-Host '[ERROR] Failed to compile application sources.'
    exit $LASTEXITCODE
}

$TEST_SRC     = Join-Path $PSScriptRoot 'src\test\java\com\bupt\ta\nomvn\NoMvnWorkflowChecks.java'
$TEST_CLASSES = Join-Path $PSScriptRoot 'out\test-classes'

if (-not (Test-Path $TEST_SRC)) {
    Write-Host "[ERROR] Test source not found: $TEST_SRC"
    exit 1
}

Write-Host '[2/3] Compile no-maven smoke checks...'
if (Test-Path $TEST_CLASSES) { Remove-Item -Recurse -Force $TEST_CLASSES }
New-Item -ItemType Directory -Force -Path $TEST_CLASSES | Out-Null

$compileCp = "$OUT_CLASSES;$SERVLET_JAR"
& $JAVAC -source 11 -target 11 -encoding UTF-8 -cp $compileCp -d $TEST_CLASSES $TEST_SRC
if ($LASTEXITCODE -ne 0) {
    Write-Host '[ERROR] Failed to compile no-maven smoke checks.'
    exit $LASTEXITCODE
}

Write-Host '[3/3] Run no-maven smoke checks...'
$runCp = "$OUT_CLASSES;$TEST_CLASSES;$SERVLET_JAR"
& $JAVA -ea -cp $runCp com.bupt.ta.nomvn.NoMvnWorkflowChecks
exit $LASTEXITCODE
