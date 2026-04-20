package org.example.cli;

import org.example.exceptions.input.InvalidUserInputException;
import org.example.model.apitype.ApiType;
import org.example.model.format.FileFormat;
import org.example.model.modes.FileMode;
import org.example.model.modes.PollingMode;
import org.example.model.modes.ReadMode;
import org.example.model.modes.RunMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Menu {
    private final Scanner scanner;

    public Menu() {
        this.scanner = new Scanner(System.in);
    }

    public RunMode selectRunMode() throws InvalidUserInputException {
        return selectEnum("run mode", RunMode.class, "invalid run mode");
    }

    public ApiType selectApi() throws InvalidUserInputException {
        return selectEnum("API type", ApiType.class, "invalid API type");
    }

    public FileFormat selectFileFormat() throws InvalidUserInputException {
        return selectEnum("file format", FileFormat.class, "invalid file format");
    }

    public FileMode selectFileMode() throws InvalidUserInputException {
        return selectEnum("file mode", FileMode.class, "invalid file mode");
    }

    public ReadMode selectReadMode() throws InvalidUserInputException{
        return selectEnum("read mode", ReadMode.class, "invalid read mode");
    }

    public PollingMode selectPollingMode() throws InvalidUserInputException {
        return selectEnum("polling mode", PollingMode.class, "invalid polling mode");
    }

    private <T extends Enum<T>> T selectEnum(String title, Class<T> enumClass, String errorMsg)
            throws InvalidUserInputException {

        System.out.println("\n===== Choose " + title + " =====");
        T[] values = enumClass.getEnumConstants();
        printOptions(values);

        int choice = (int) chooseNumber(values.length, 1, errorMsg);

        return values[choice - 1];
    }

    private <T> void printOptions(T[] items) {
        for (int i = 0; i < items.length; i++) {
            System.out.println((i + 1) + ": " + items[i]);
        }

        System.out.println("Enter the number...");
    }

    private long chooseNumber(long size, long minNum, String msg) throws InvalidUserInputException {
        int attempts = 3;

        while (attempts > 0) {
            if (!scanner.hasNextInt()) {
                attempts--;
                scanner.next();
                System.out.println("Enter the number. Attempts left: " + attempts);
                continue;
            }

            long choice = scanner.nextInt();
            scanner.nextLine();

            if (choice < minNum || choice > size) {
                attempts--;
                System.out.println(msg + " Attempts left: " + attempts);
            } else {
                return choice;
            }
        }

        throw new InvalidUserInputException(msg);
    }

    public String getFileName() throws InvalidUserInputException {
        System.out.println("\nEnter the file name: ");

        String fileName = scanner.nextLine().trim();
        if (fileName.isEmpty()) {
            throw new InvalidUserInputException("File name cannot be empty");
        }

        return fileName;
    }

    public boolean wantToDo(String msg) {
        System.out.println(msg + " (y/n):");

        return scanner.nextLine().trim().equalsIgnoreCase("y");
    }

    public int getThreadCount() throws InvalidUserInputException {
        System.out.print("\nMax parallel threads (n >= 1): ");
        return (int) chooseNumber(100, 1, "invalid thread count");
    }

    public long getIntervalSeconds() throws InvalidUserInputException {
        System.out.print("Polling interval in seconds (t >= 0): ");

        return chooseNumber(Long.MAX_VALUE, 0, "invalid interval");
    }

    public List<ApiType> selectMultipleApis() throws InvalidUserInputException {
        System.out.println("\nSelect APIs");

        ApiType[] values = ApiType.values();
        printOptions(values);
        System.out.println("Example: 1 3");

        String input = scanner.nextLine().trim();
        List<ApiType> apis = new ArrayList<>();
        for (String part : input.split(" ")) {
            try {
                int idx = Integer.parseInt(part.trim()) - 1;
                if (idx < 0 || idx >= values.length) {
                    throw new InvalidUserInputException("Invalid API number: " + (idx + 1));
                }
                apis.add(values[idx]);
            } catch (NumberFormatException e) {
                throw new InvalidUserInputException("Invalid API number");
            }
        }
        if (apis.isEmpty()) throw new InvalidUserInputException("No APIs selected");
        return apis;
    }

    public void close() {
        scanner.close();
    }
}
