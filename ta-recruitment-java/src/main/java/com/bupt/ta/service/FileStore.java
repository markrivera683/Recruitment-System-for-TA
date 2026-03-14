package com.bupt.ta.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileStore {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path baseDir;

    public FileStore(Path baseDir) {
        this.baseDir = baseDir;
    }

    public <T> List<T> readList(String fileName, Type listType) throws IOException {
        Path p = baseDir.resolve(fileName);
        if (!Files.exists(p)) {
            return new ArrayList<>();
        }
        try (Reader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            List<T> list = gson.fromJson(r, listType);
            return list != null ? list : new ArrayList<>();
        }
    }

    public <T> void writeList(String fileName, List<T> items) throws IOException {
        Path p = baseDir.resolve(fileName);
        Files.createDirectories(p.getParent());
        try (Writer w = Files.newBufferedWriter(p, StandardCharsets.UTF_8)) {
            gson.toJson(items, w);
        }
    }

    public static <T> Type listType(Class<T> clazz) {
        return TypeToken.getParameterized(List.class, clazz).getType();
    }
}
