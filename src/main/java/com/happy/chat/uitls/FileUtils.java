package com.happy.chat.uitls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.core.io.ClassPathResource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtils {
    public static String getFileContent(String fileName) {
        StringBuilder sb = new StringBuilder("");
        try {
            ClassPathResource classPathResource = new ClassPathResource(fileName);
            InputStream inputStream = classPathResource.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String str;
            while ((str = in.readLine()) != null) {
                sb.append(str).append("\n");
            }
        } catch (IOException e) {
            log.error("getFileContent exception", e);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(getFileContent("mail.html"));
    }
}
