package org.example;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String[] IPS = {"IP1", "IP2", "IP3", "IP4", "IP5"};
    private static final String USER = "username";
    private static final String PASSWORD = "password";
    private static final String[] COMMANDS = {
            "grep -b 5 ~/tmp/aa.log",
            "grep -b 5 ~/tmp/bb.log"
    };

    public static void main(String[] args) throws Exception {
        List<String> results = new ArrayList<>();
        JSch jsch = new JSch();

        for (String ip : IPS) {
            Session session = jsch.getSession(USER, ip);
            session.setPassword(PASSWORD);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            for (String command : COMMANDS) {
                ChannelExec channel = (ChannelExec) session.openChannel("exec");
                channel.setCommand(command);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                channel.setOutputStream(outputStream);
                channel.connect();

                while (!channel.isClosed()) {
                    Thread.sleep(100);
                }
                channel.disconnect();
                results.add(outputStream.toString());
            }
            session.disconnect();
        }

        writeToExcel(results);
    }

    private static void writeToExcel(List<String> results) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("SFTP Results");
        int rowIndex = 0;

        for (String result : results) {
            Row row = sheet.createRow(rowIndex++);
            Cell cell = row.createCell(0);
            cell.setCellValue(result);
        }

        try (FileOutputStream outputStream = new FileOutputStream("SFTPResults.xlsx")) {
            workbook.write(outputStream);
        }
        workbook.close();
    }
}
