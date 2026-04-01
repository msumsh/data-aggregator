package org.example.cli;

import org.example.exceptions.InvalidUserInputException;
import org.example.model.*;

import java.util.Scanner;

public class Menu implements AutoCloseable {
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

    private <T extends Enum<T>> T selectEnum(String title, Class<T> enumClass, String errorMsg)
            throws InvalidUserInputException {

        System.out.println("\n===== Choose " + title + " =====");
        T[] values = enumClass.getEnumConstants();
        printOptions(values);

        int choice = chooseOption(values.length, errorMsg);

        return values[choice - 1];
    }

    private <T> void printOptions(T[] items) {
        for (int i = 0; i < items.length; i++) {
            System.out.println((i + 1) + ": " + items[i]);
        }

        System.out.println("Enter the number...");
    }

    private int chooseOption(int size, String msg) throws InvalidUserInputException {
        int attempts = 3;

        while (attempts > 0) {
            if (!scanner.hasNextInt()) {
                attempts--;
                scanner.next();
                System.out.println("Enter the number. Attempts left: " + attempts);
                continue;
            }

            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice < 1 || choice > size) {
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

    public boolean wantToContinue() {
        System.out.println("Do you want to continue? (y/n)");

        return scanner.nextLine().trim().equalsIgnoreCase("y");
    }

    public boolean wantToViewFile() {
        System.out.print("\nDo you want to read the file? (y/n): ");

        return scanner.nextLine().trim().equalsIgnoreCase("y");
    }

    @Override
    public void close() {
        scanner.close();
    }
}
