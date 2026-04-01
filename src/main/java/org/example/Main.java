package org.example;

import org.example.cli.Menu;
import org.example.exceptions.*;
import org.example.model.*;
import org.example.modes.Auto;
import org.example.modes.Interactive;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try (Menu menu = new Menu()) {
            RunMode mode = menu.selectRunMode();
            if (mode == RunMode.AUTO) {
                Auto.runAuto(args);
            } else {
                Interactive.runInteractive(menu);
            }
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
            System.exit(1);
        } catch (InvalidUserInputException e) {
            System.err.println("Invalid user input: " + e.getMessage());
            System.exit(1);
        } catch (SavingJsonException e) {
            System.err.println("Saving to JSON error: " + e.getMessage());
            System.exit(1);
        } catch (SavingCsvException e) {
            System.err.println("Saving to CSV error: " + e.getMessage());
            System.exit(1);
        }
    }
}