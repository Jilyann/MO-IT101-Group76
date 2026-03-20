/*
 * MotorPH Milestone 2 - Payroll System
 * Single Java file, no OOP - per MS2 requirements
 *
 * Login: employee / payroll_staff | Password: 12345
 * Hours: 8:00 AM - 5:00 PM, 8:05 on time, 1hr lunch, no overtime
 * Cutoffs: 1-15 (first), 16-end (second)
 * Deductions on 2nd cutoff only, based on 1st+2nd gross combined (MotorPH matrices)
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

public class MotorPH_MS2 {

    static final String EMPLOYEE_CSV = "data/MotorPH_Employee Data - Employee Details.csv";
    static final String ATTENDANCE_CSV = "data/MotorPH_Employee Data - Attendance Record.csv";

    static final int START_MONTH = 6;
    static final int END_MONTH = 12;
    static final int CUTOFF_DAY = 15;
    static final double WORK_START = 8.0;
    static final double WORK_END = 17.0;
    static final double GRACE_END = 8.0 + 5.0 / 60;
    static final double MAX_HOURS_PER_DAY = 8.0;

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);

        System.out.println("=== MotorPH Payroll System ===");
        System.out.print("Enter username: ");
        String username = scan.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scan.nextLine().trim();

        if (!login(username, password)) {
            System.out.println("Incorrect username and/or password.");
            return;
        }

        if ("employee".equals(username)) {
            employeeMenu(scan);
        } else {
            payrollStaffMenu(scan);
        }
        scan.close();
    }

    static boolean login(String username, String password) {
        return ("employee".equals(username) || "payroll_staff".equals(username)) && "12345".equals(password);
    }

    static void employeeMenu(Scanner scan) {
        while (true) {
            System.out.println("\n--- Employee Menu ---");
            System.out.println("1. Enter employee number");
            System.out.println("2. Exit");
            System.out.print("Choice: ");
            String choice = scan.nextLine().trim();

            if ("2".equals(choice)) {
                System.out.println("Exiting.");
                return;
            }
            if ("1".equals(choice)) {
                System.out.print("Enter employee number: ");
                String empNum = scan.nextLine().trim();
                lookupEmployee(empNum);
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    static void lookupEmployee(String empNum) {
        Map<String, String[]> employees = loadEmployeeDetails();
        if (employees == null) return;

        String[] emp = employees.get(empNum);
        if (emp == null) {
            System.out.println("Employee number does not exist.");
            return;
        }
        System.out.println("Employee #: " + emp[0]);
        System.out.println("Name: " + emp[2] + " " + emp[1]);
        System.out.println("Birthday: " + emp[3]);
    }

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

    /** Tries data/ first, then project root - avoids duplicated file lookup. */
    static File getFile(String primaryPath, String fallbackPath) {
        File f = new File(primaryPath);
        if (!f.exists()) f = new File(fallbackPath);
        return f;
    }

    static Map<String, String[]> loadEmployeeDetails() {
        Map<String, String[]> map = new HashMap<>();
        File f = getFile(EMPLOYEE_CSV, "MotorPH_Employee Data - Employee Details.csv");
        if (!f.exists()) {
            System.out.println("Error: Employee Details CSV not found.");
            return null;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                List<String> row = parseCSVLine(line);
                if (row.size() >= 19) {
                    String empNum = row.get(0);
                    String lastName = row.get(1);
                    String firstName = row.get(2);
                    String birthday = row.get(3);
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

    static List<String[]> loadAttendance() {
        List<String[]> list = new ArrayList<>();
        File f = getFile(ATTENDANCE_CSV, "MotorPH_Employee Data - Attendance Record.csv");
        if (!f.exists()) {
            System.out.println("Error: Attendance Record CSV not found.");
            return null;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    list.add(new String[]{parts[0].trim(), parts[3].trim(), parts[4].trim(), parts[5].trim()});
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading Attendance: " + e.getMessage());
            return null;
        }
        return list;
    }

    /** Handles commas inside quoted fields (e.g. address). */
    static List<String> parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') inQuotes = !inQuotes;
            else if (c == ',' && !inQuotes) {
                result.add(sb.toString().trim());
                sb.setLength(0);
            } else sb.append(c);
        }
        result.add(sb.toString().trim());
        return result;
    }

    /** Returns decimal hours; validates format and range to avoid crash. */
    static double parseTimeToHours(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) return 0;
        String[] parts = timeStr.trim().split(":");
        if (parts.length < 2) return 0;
        try {
            int hour = Integer.parseInt(parts[0].trim());
            int minute = Integer.parseInt(parts[1].trim());
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return 0;
            return hour + minute / 60.0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** 8:05 = on time (count from 8:00); 1hr lunch; cap 8hrs, no overtime. */
    static double computeHoursWorked(String logIn, String logOut) {
        double timeIn = parseTimeToHours(logIn);
        double timeOut = parseTimeToHours(logOut);
        if (timeOut <= timeIn) return 0;

        double effectiveStart = (timeIn <= GRACE_END) ? WORK_START : timeIn;
        double effectiveEnd = (timeOut >= WORK_END) ? WORK_END : timeOut;
        double rawHours = effectiveEnd - effectiveStart;
        if (effectiveStart < 12 && effectiveEnd > 13) rawHours -= 1;
        return Math.min(MAX_HOURS_PER_DAY, Math.max(0, rawHours));
    }

    /** 1-15 = cutoff 1, 16-end = cutoff 2; validates to avoid crash. */
    static int getCutoff(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return 0;
        String[] parts = dateStr.trim().split("/");
        if (parts.length < 2) return 0;
        try {
            int day = Integer.parseInt(parts[1].trim());
            return (day <= CUTOFF_DAY) ? 1 : 2;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static int getMonth(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return 0;
        String[] parts = dateStr.trim().split("/");
        if (parts.length < 1) return 0;
        try {
            return Integer.parseInt(parts[0].trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Data only: hours per month/cutoff; separate from printing. */
    static double[][] computeHoursPerMonth(String empNum, List<String[]> attendance) {
        double[][] hours = new double[13][3];
        for (String[] row : attendance) {
            if (!row[0].equals(empNum)) continue;
            String dateStr = row[1];
            int month = getMonth(dateStr);
            if (month < START_MONTH || month > END_MONTH) continue;
            int cutoff = getCutoff(dateStr);
            double hrs = computeHoursWorked(row[2], row[3]);
            hours[month][cutoff] += hrs;
        }
        return hours;
    }

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

    static void processAllEmployees() {
        Map<String, String[]> employees = loadEmployeeDetails();
        List<String[]> attendance = loadAttendance();
        if (employees == null || attendance == null) return;

        List<String> empNumbers = new ArrayList<>(employees.keySet());
        Collections.sort(empNumbers, (a, b) -> Integer.parseInt(a) - Integer.parseInt(b));
        for (String empNum : empNumbers) {
            String[] emp = employees.get(empNum);
            double hourlyRate = Double.parseDouble(emp[4]);
            printPayrollForEmployee(emp[0], emp[2] + " " + emp[1], emp[3], hourlyRate, attendance);
        }
    }

    static void printPayrollForEmployee(String empNum, String name, String birthday, double hourlyRate, List<String[]> attendance) {
        double[][] hours = computeHoursPerMonth(empNum, attendance);
        String[] monthNames = {"", "Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec"};

        System.out.println("\n========================================");
        System.out.println("Employee #: " + empNum);
        System.out.println("Employee Name: " + name);
        System.out.println("Birthday: " + birthday);
        System.out.println("========================================");

        for (int m = START_MONTH; m <= END_MONTH; m++) {
            double firstCutoffHours = hours[m][1];
            double secondCutoffHours = hours[m][2];
            double gross1 = firstCutoffHours * hourlyRate;
            double gross2 = secondCutoffHours * hourlyRate;
            double monthlyGross = gross1 + gross2;

            System.out.println("\n--- " + monthNames[m] + " 2024 ---");

            // First cutoff: no deductions per requirement
            System.out.println("First Cutoff (1-15):");
            System.out.println("  Hours: " + firstCutoffHours);
            System.out.println("  Gross: " + gross1);
            System.out.println("  Net: " + gross1);

            // Deductions based on monthly gross; applied on 2nd cutoff only
            double sss = 0, phil = 0, pagibig = 0, tax = 0;
            if (monthlyGross > 0) {
                sss = computeSSS(monthlyGross);
                phil = computePhilHealth(monthlyGross);
                pagibig = computePagIbig(monthlyGross);
                double totalDed = sss + phil + pagibig;
                double taxable = monthlyGross - totalDed;
                tax = computeIncomeTax(taxable);
            }
            double totalDeductions = sss + phil + pagibig + tax;
            double net2 = gross2 - totalDeductions;

            System.out.println("Second Cutoff (16-end):");
            System.out.println("  Hours: " + secondCutoffHours);
            System.out.println("  Gross: " + gross2);
            System.out.println("  SSS: " + sss);
            System.out.println("  PhilHealth: " + phil);
            System.out.println("  Pag-IBIG: " + pagibig);
            System.out.println("  Withholding Tax: " + tax);
            System.out.println("  Total Deductions: " + totalDeductions);
            System.out.println("  Net: " + net2);
        }
    }

    /** MotorPH SSS matrix - bracketed monthly compensation. */
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
        return 1125.00;
    }

    /** MotorPH: 3% premium, employee 50%; min 300, max 1800. */
    static double computePhilHealth(double grossSalary) {
        if (grossSalary <= 0) return 0;
        double premium;
        if (grossSalary <= 10000) premium = 300;
        else if (grossSalary >= 60000) premium = 1800;
        else premium = grossSalary * 0.03;
        return premium * 0.5;
    }

    /** MotorPH: 1% (1K-1.5K), 2% (>1.5K), max 100. */
    static double computePagIbig(double grossSalary) {
        if (grossSalary <= 0) return 0;
        double rate = (grossSalary >= 1000 && grossSalary <= 1500) ? 0.01 : 0.02;
        return Math.min(grossSalary * rate, 100);
    }

    /** BIR tax table - taxable = gross minus SSS/PhilHealth/Pag-IBIG. */
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
