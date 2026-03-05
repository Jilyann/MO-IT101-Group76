/*
 * MotorPH Milestone 2 - Payroll System
 * 
 * This program implements a semi-monthly payroll system for MotorPH company.
 * It supports two user types: employee (for viewing own info) and payroll staff
 * (for processing payroll). Uses MotorPH matrices for SSS, PhilHealth, Pag-IBIG,
 * and Withholding Tax computations.
 * 
 * Requirements met:
 * - Single Java file, no OOP
 * - Login: employee/payroll_staff, password: 12345
 * - Hours: 8:00 AM - 5:00 PM, 8:05 AM considered on time, 1-hour lunch, no overtime
 * - Cutoffs: 1-15 (first), 16-end (second)
 * - Deductions applied on second cutoff only, based on combined 1st+2nd gross
 * 
 * @author Jilianne
 */
package com.mycompany.motorphms2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Main class for MotorPH Payroll System.
 * Handles login, employee lookup, and payroll processing.
 */
public class MotorPH_MS2 {

    // Paths to CSV files - program looks in 'data' folder inside project directory
    static final String EMPLOYEE_CSV = "data/MotorPH_Employee Data - Employee Details.csv";
    static final String ATTENDANCE_CSV = "data/MotorPH_Employee Data - Attendance Record.csv";

    /**
     * Main method - entry point of the program.
     * Prompts user for login credentials, validates them, then shows appropriate menu.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);

        // Display welcome message and prompt for credentials
        System.out.println("=== MotorPH Payroll System ===");
        System.out.print("Enter username: ");
        String username = scan.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scan.nextLine().trim();

        // Validate login - if wrong, display message and exit program
        if (!login(username, password)) {
            System.out.println("Incorrect username and/or password.");
            return;
        }

        // Route to correct menu based on user type
        if ("employee".equals(username)) {
            employeeMenu(scan);
        } else {
            payrollStaffMenu(scan);
        }
        scan.close();
    }

    /**
     * Validates login credentials.
     * Accepts only 'employee' or 'payroll_staff' with password '12345'.
     * 
     * @param username the username entered by user
     * @param password the password entered by user
     * @return true if credentials are correct, false otherwise
     */
    static boolean login(String username, String password) {
        return ("employee".equals(username) || "payroll_staff".equals(username)) && "12345".equals(password);
    }

    /**
     * Employee menu - allows employee to lookup their info by employee number.
     * Option 1: Enter employee number to view details
     * Option 2: Exit the program
     * 
     * @param scan Scanner object for user input
     */
    static void employeeMenu(Scanner scan) {
        while (true) {
            System.out.println("\n--- Employee Menu ---");
            System.out.println("1. Enter employee number");
            System.out.println("2. Exit");
            System.out.print("Choice: ");
            String choice = scan.nextLine().trim();

            // Exit option - user chooses to leave the program
            if ("2".equals(choice)) {
                System.out.println("Exiting.");
                return;
            }
            // Lookup option - user enters employee number to view their details
            if ("1".equals(choice)) {
                System.out.print("Enter employee number: ");
                String empNum = scan.nextLine().trim();
                lookupEmployee(empNum);
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    /**
     * Looks up an employee by employee number and displays their info.
     * Per process flow: displays Employee Number, Employee Name, Birthday.
     * Shows error message if employee number does not exist.
     * 
     * @param empNum the employee number to search for
     */
    static void lookupEmployee(String empNum) {
        Map<String, String[]> employees = loadEmployeeDetails();
        if (employees == null) return;

        // Check if employee exists in the map
        String[] emp = employees.get(empNum);
        if (emp == null) {
            System.out.println("Employee number does not exist.");
            return;
        }
        // Display employee info per process flow: Employee Number, Employee Name, Birthday
        System.out.println("Employee Number: " + emp[0]);
        System.out.println("Employee Name: " + emp[2] + " " + emp[1]);
        System.out.println("Birthday: " + emp[3]);
    }

    /**
     * Payroll staff menu - allows payroll staff to process payroll.
     * Option 1: Process Payroll (submenu)
     * Option 2: Exit
     * 
     * @param scan Scanner object for user input
     */
    static void payrollStaffMenu(Scanner scan) {
        while (true) {
            System.out.println("\n--- Payroll Staff Menu ---");
            System.out.println("1. Process Payroll");
            System.out.println("2. Exit");
            System.out.print("Choice: ");
            String choice = scan.nextLine().trim();

            if ("2".equals(choice)) {
                System.out.println("Exiting.");
                return;
            }
            if ("1".equals(choice)) {
                processPayrollSubMenu(scan);
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    /**
     * Process Payroll submenu - choose to process one employee or all employees.
     * Option 1: One employee - enter specific employee number
     * Option 2: All employees - process payroll for everyone (sorted by employee number)
     * Option 3: Exit - go back to Payroll Staff menu
     * 
     * @param scan Scanner object for user input
     */
    static void processPayrollSubMenu(Scanner scan) {
        while (true) {
            System.out.println("\n--- Process Payroll ---");
            System.out.println("1. One employee");
            System.out.println("2. All employees");
            System.out.println("3. Exit");
            System.out.print("Choice: ");
            String choice = scan.nextLine().trim();

            if ("3".equals(choice)) return;
            if ("1".equals(choice)) {
                System.out.print("Enter employee number: ");
                String empNum = scan.nextLine().trim();
                processOneEmployee(empNum);
            } else if ("2".equals(choice)) {
                processAllEmployees();
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    /**
     * Loads employee details from CSV file into a Map.
     * Key = employee number, Value = array of [empNum, lastName, firstName, birthday, hourlyRate]
     * Handles quoted fields in CSV (e.g. addresses with commas).
     * 
     * @return Map of employee number to employee data, or null if file not found/error
     */
    static Map<String, String[]> loadEmployeeDetails() {
        Map<String, String[]> map = new HashMap<>();
        // Try data folder first, then project root (fallback)
        File f = new File(EMPLOYEE_CSV);
        if (!f.exists()) {
            f = new File("MotorPH_Employee Data - Employee Details.csv");
        }
        if (!f.exists()) {
            System.out.println("Error: Employee Details CSV not found.");
            return null;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine(); // Skip header row
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                // Parse CSV line - handles commas inside quotes (e.g. address field)
                List<String> row = parseCSVLine(line);
                if (row.size() >= 19) {
                    String empNum = row.get(0);
                    String lastName = row.get(1);
                    String firstName = row.get(2);
                    String birthday = row.get(3);
                    // Hourly Rate is last column - remove any commas for number parsing
                    String hourlyRate = row.get(row.size() - 1).replace(",", "").trim();
                    map.put(empNum, new String[]{empNum, lastName, firstName, birthday, hourlyRate});
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading Employee Details: " + e.getMessage());
            return null;
        }
        return map;
    }

    /**
     * Loads attendance records from CSV file.
     * Each record: employee number, date, log in time, log out time.
     * 
     * @return List of attendance records, or null if file not found/error
     */
    static List<String[]> loadAttendance() {
        List<String[]> list = new ArrayList<>();
        File f = new File(ATTENDANCE_CSV);
        if (!f.exists()) {
            f = new File("MotorPH_Employee Data - Attendance Record.csv");
        }
        if (!f.exists()) {
            System.out.println("Error: Attendance Record CSV not found.");
            return null;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    // Store: empNum (0), date (1), logIn (2), logOut (3)
                    list.add(new String[]{
                        parts[0].trim(),
                        parts[3].trim(),
                        parts[4].trim(),
                        parts[5].trim()
                    });
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading Attendance: " + e.getMessage());
            return null;
        }
        return list;
    }

    /**
     * Parses a CSV line that may contain commas inside quoted fields.
     * Example: "Street, City" stays as one field, not split by the comma inside.
     * 
     * @param line one line from CSV file
     * @return List of field values
     */
    static List<String> parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        // Loop through each character - track when we're inside quotes
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes; // Toggle quote state on/off
            } else if (c == ',' && !inQuotes) {
                // Only split on comma when NOT inside quotes
                result.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString().trim());
        return result;
    }

    /**
     * Converts time string (e.g. "8:59", "18:31") to decimal hours.
     * Used for calculating hours worked.
     * 
     * @param timeStr time in format "H:mm" or "HH:mm"
     * @return decimal hours (e.g. 8.98 for 8:59)
     */
    static double parseTimeToHours(String timeStr) {
        String[] p = timeStr.split(":");
        if (p.length < 2) return 0;
        int h = Integer.parseInt(p[0].trim());
        int m = Integer.parseInt(p[1].trim());
        return h + m / 60.0;
    }

    /**
     * Computes hours worked for a single day based on log in and log out.
     * Rules: 8:00 AM - 5:00 PM work hours, 8:05 AM = on time, 1-hour lunch, max 8 hours (no overtime).
     * If employee arrives by 8:05, count from 8:00. If leaves at/after 5:00 PM, count until 5:00.
     * 
     * @param logIn  time in (e.g. "8:59")
     * @param logOut time out (e.g. "18:31")
     * @return hours worked (0 to 8)
     */
    static double computeHoursWorked(String logIn, String logOut) {
        double timeIn = parseTimeToHours(logIn);
        double timeOut = parseTimeToHours(logOut);
        if (timeOut <= timeIn) return 0;

        // 8:05 AM = 8.0833... - if arrived by 8:05, treat as started at 8:00 (on time)
        double effectiveStart = (timeIn <= 8 + 5.0 / 60) ? 8.0 : timeIn;
        // If left at or after 5:00 PM, count until 5:00 only (no overtime)
        double effectiveEnd = (timeOut >= 17.0) ? 17.0 : timeOut;

        double rawHours = effectiveEnd - effectiveStart;
        // Subtract 1 hour for lunch break if work spans 12:00-1:00 PM
        if (effectiveStart < 12 && effectiveEnd > 13) {
            rawHours -= 1;
        }
        // Cap at 8 hours - no overtime allowed
        return Math.min(8, Math.max(0, rawHours));
    }

    /**
     * Determines which cutoff a date falls into.
     * Cutoff 1: days 1-15 of month
     * Cutoff 2: days 16 to end of month
     * 
     * @param dateStr date in format MM/dd/yyyy
     * @return 1 for first cutoff, 2 for second cutoff
     */
    static int getCutoff(String dateStr) {
        String[] p = dateStr.split("/");
        if (p.length < 2) return 0;
        int day = Integer.parseInt(p[1].trim());
        return (day <= 15) ? 1 : 2;
    }

    /**
     * Extracts month number from date string.
     * 
     * @param dateStr date in format MM/dd/yyyy
     * @return month (1-12)
     */
    static int getMonth(String dateStr) {
        String[] p = dateStr.split("/");
        if (p.length < 1) return 0;
        return Integer.parseInt(p[0].trim());
    }

    /**
     * Processes payroll for one employee by employee number.
     * Validates employee exists, then calls print method.
     * 
     * @param empNum employee number to process
     */
    static void processOneEmployee(String empNum) {
        Map<String, String[]> employees = loadEmployeeDetails();
        List<String[]> attendance = loadAttendance();
        if (employees == null || attendance == null) return;

        if (!employees.containsKey(empNum)) {
            System.out.println("Employee number does not exist.");
            return;
        }

        String[] emp = employees.get(empNum);
        double hourlyRate = Double.parseDouble(emp[4]);
        printPayrollForEmployee(empNum, emp[2] + " " + emp[1], emp[3], hourlyRate, attendance);
    }

    /**
     * Processes payroll for all employees in the system.
     * Employees are displayed in ascending order by employee number (10001, 10002, 10003...).
     */
    static void processAllEmployees() {
        Map<String, String[]> employees = loadEmployeeDetails();
        List<String[]> attendance = loadAttendance();
        if (employees == null || attendance == null) return;

        // Get all employee numbers and sort them numerically (10001, 10002, 10003...)
        List<String> empNumbers = new ArrayList<>(employees.keySet());
        Collections.sort(empNumbers, (a, b) -> Integer.parseInt(a) - Integer.parseInt(b));

        // Process each employee in sorted order
        for (String empNum : empNumbers) {
            String[] emp = employees.get(empNum);
            double hourlyRate = Double.parseDouble(emp[4]);
            printPayrollForEmployee(emp[0], emp[2] + " " + emp[1], emp[3], hourlyRate, attendance);
        }
    }

    /**
     * Prints payroll summary for one employee for June-December 2024.
     * Per MS2 format: Employee #, Employee Name, Birthday, then cutoff details.
     * First cutoff: Hours, Gross, Net (no deductions).
     * Second cutoff: Hours, Gross, SSS, PhilHealth, Pag-IBIG, Tax, Total Deductions, Net.
     * 
     * @param empNum      employee number
     * @param name        employee full name
     * @param birthday    employee birthday
     * @param hourlyRate  hourly rate (used for calculation, not displayed)
     * @param attendance  list of all attendance records
     */
    static void printPayrollForEmployee(String empNum, String name, String birthday, double hourlyRate, List<String[]> attendance) {
        // hours[month][cutoff] = total hours for that month and cutoff
        double[][] hours = new double[13][3];
        String[] monthNames = {"", "Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec"};

        // Sum hours for each month (6-12 = June-December) and cutoff (1 or 2)
        for (String[] row : attendance) {
            if (!row[0].equals(empNum)) continue;
            String dateStr = row[1];
            int month = getMonth(dateStr);
            if (month < 6 || month > 12) continue; // June-December only

            int cutoff = getCutoff(dateStr);
            double hrs = computeHoursWorked(row[2], row[3]);
            hours[month][cutoff] += hrs;
        }

        // Print header - per MS2 format: Employee #, Employee Name, Birthday
        System.out.println("\n========================================");
        System.out.println("Employee #: " + empNum);
        System.out.println("Employee Name: " + name);
        System.out.println("Birthday: " + birthday);
        System.out.println("========================================");

        // For each month June to December
        for (int m = 6; m <= 12; m++) {
            double h1 = hours[m][1];
            double h2 = hours[m][2];
            double gross1 = h1 * hourlyRate;
            double gross2 = h2 * hourlyRate;
            double monthlyGross = gross1 + gross2; // Combined for deduction computation

            System.out.println("\n--- " + monthNames[m] + " 2024 ---");

            // FIRST CUTOFF: Hours, Gross, Net (no deductions)
            System.out.println("First Cutoff (1-15):");
            System.out.println("  Hours: " + h1);
            System.out.println("  Gross: " + gross1);
            System.out.println("  Net: " + gross1);

            // SECOND CUTOFF: Compute deductions based on MONTHLY gross (1st+2nd combined)
            // Per requirement: add 1st+2nd gross first, then apply deductions on 2nd cutoff only
            double sss = 0, phil = 0, pagibig = 0, tax = 0;
            if (monthlyGross > 0) {
                sss = computeSSS(monthlyGross);
                phil = computePhilHealth(monthlyGross);
                pagibig = computePagIbig(monthlyGross);
                double totalDed = sss + phil + pagibig;
                double taxable = monthlyGross - totalDed; // Taxable = gross minus SSS, PhilHealth, Pag-IBIG
                tax = computeIncomeTax(taxable);
            }

            double totalDeductions = sss + phil + pagibig + tax;
            double net2 = gross2 - totalDeductions; // All deductions come from 2nd cutoff pay

            System.out.println("Second Cutoff (16-end):");
            System.out.println("  Hours: " + h2);
            System.out.println("  Gross: " + gross2);
            System.out.println("  SSS: " + sss);
            System.out.println("  PhilHealth: " + phil);
            System.out.println("  Pag-IBIG: " + pagibig);
            System.out.println("  Withholding Tax: " + tax);
            System.out.println("  Total Deductions: " + totalDeductions);
            System.out.println("  Net: " + net2);
        }
    }

    /**
     * Computes SSS contribution based on monthly compensation (MotorPH SSS matrix).
     * Uses bracketed ranges from official SSS contribution schedule.
     * 
     * @param grossSalary monthly gross salary (combined 1st+2nd cutoff)
     * @return SSS contribution amount
     */
    static double computeSSS(double grossSalary) {
        if (grossSalary <= 0) return 0;
        if (grossSalary < 3250) return 135.00;
        if (grossSalary < 3750) return 157.50;
        if (grossSalary < 4250) return 180.00;
        if (grossSalary < 4750) return 202.50;
        if (grossSalary < 5250) return 225.00;
        if (grossSalary < 5750) return 247.50;
        if (grossSalary < 6250) return 270.00;
        if (grossSalary < 6750) return 292.50;
        if (grossSalary < 7250) return 315.00;
        if (grossSalary < 7750) return 337.50;
        if (grossSalary < 8250) return 360.00;
        if (grossSalary < 8750) return 382.50;
        if (grossSalary < 9250) return 405.00;
        if (grossSalary < 9750) return 427.50;
        if (grossSalary < 10250) return 450.00;
        if (grossSalary < 10750) return 472.50;
        if (grossSalary < 11250) return 495.00;
        if (grossSalary < 11750) return 517.50;
        if (grossSalary < 12250) return 540.00;
        if (grossSalary < 12750) return 562.50;
        if (grossSalary < 13250) return 585.00;
        if (grossSalary < 13750) return 607.50;
        if (grossSalary < 14250) return 630.00;
        if (grossSalary < 14750) return 652.50;
        if (grossSalary < 15250) return 675.00;
        if (grossSalary < 15750) return 697.50;
        if (grossSalary < 16250) return 720.00;
        if (grossSalary < 16750) return 742.50;
        if (grossSalary < 17250) return 765.00;
        if (grossSalary < 17750) return 787.50;
        if (grossSalary < 18250) return 810.00;
        if (grossSalary < 18750) return 832.50;
        if (grossSalary < 19250) return 855.00;
        if (grossSalary < 19750) return 877.50;
        if (grossSalary < 20250) return 900.00;
        if (grossSalary < 20750) return 922.50;
        if (grossSalary < 21250) return 945.00;
        if (grossSalary < 21750) return 967.50;
        if (grossSalary < 22250) return 990.00;
        if (grossSalary < 22750) return 1012.50;
        if (grossSalary < 23250) return 1035.00;
        if (grossSalary < 23750) return 1057.50;
        if (grossSalary < 24250) return 1080.00;
        if (grossSalary < 24750) return 1102.50;
        return 1125.00; // 24,750 and over
    }

    /**
     * Computes PhilHealth contribution - employee share 50% of premium (MotorPH matrix).
     * Premium: 3% of monthly salary, min 300, max 1800.
     * 
     * @param grossSalary monthly gross salary
     * @return PhilHealth employee contribution (half of premium)
     */
    static double computePhilHealth(double grossSalary) {
        if (grossSalary <= 0) return 0;
        double premium;
        if (grossSalary <= 10000) premium = 300;
        else if (grossSalary >= 60000) premium = 1800;
        else premium = grossSalary * 0.03;
        return premium * 0.5; // Employee share is 50%
    }

    /**
     * Computes Pag-IBIG contribution (MotorPH matrix).
     * 1% for salary 1,000-1,500; 2% for over 1,500; maximum 100.
     * 
     * @param grossSalary monthly gross salary
     * @return Pag-IBIG contribution (capped at 100)
     */
    static double computePagIbig(double grossSalary) {
        if (grossSalary <= 0) return 0;
        double rate = (grossSalary >= 1000 && grossSalary <= 1500) ? 0.01 : 0.02;
        return Math.min(grossSalary * rate, 100); // Max 100
    }

    /**
     * Computes Withholding Tax based on taxable income (MotorPH/BIR tax table).
     * Taxable income = gross minus SSS, PhilHealth, Pag-IBIG.
     * Uses progressive tax rates.
     * 
     * @param taxableIncome salary after deducting SSS, PhilHealth, Pag-IBIG
     * @return withholding tax amount
     */
    static double computeIncomeTax(double taxableIncome) {
        if (taxableIncome <= 0) return 0;
        if (taxableIncome <= 20832) return 0;
        if (taxableIncome < 33333) return (taxableIncome - 20833) * 0.20;
        if (taxableIncome < 66667) return 2500 + (taxableIncome - 33333) * 0.25;
        if (taxableIncome < 166667) return 10833 + (taxableIncome - 66667) * 0.30;
        if (taxableIncome < 666667) return 40833.33 + (taxableIncome - 166667) * 0.32;
        return 200833.33 + (taxableIncome - 666667) * 0.35;
    }
}