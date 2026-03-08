# MotorPH MS2 Payroll

CP1 - MS2 Source Code

## Basic Payroll Program

This program reads employee information and attendance records from CSV files, handles login for employee and payroll staff users, calculates total hours worked per cutoff, applies deductions (SSS, PhilHealth, Pag-IBIG, Tax), and displays payroll summary for June to December 2024.

---

## Team Details

Jilianne Nicole Maquiling - Full development (login, employee lookup, payroll processing, hours computation, deductions, CSV handling, display formatting). Solo project.

---

## Project Plan Link

[https://docs.google.com/spreadsheets/d/1UH2ArpyVD3gc6Zp8CIMAxwTFRK0zX-mFEBTybTWuU8M/edit?usp=sharing]

---

## How the Program Works

**Imports**
- BufferedReader, FileReader - read CSV files line by line
- HashMap, ArrayList, List, Map - store employee and attendance data
- Scanner - get user input
- File - check if CSV files exist

**Main Class and Method**
- MotorPH_MS2 is the main class
- main method is the entry point, prompts for username and password

**Login**
- Accepts employee or payroll_staff with password 12345
- Wrong credentials show "Incorrect username and/or password." and exit

**Employee Menu**
- Option 1: Enter employee number to view Employee Number, Employee Name, Birthday
- Option 2: Exit
- If employee not found, displays "Employee number does not exist."

**Payroll Staff Menu**
- Option 1: Process Payroll (One employee / All employees / Exit)
- Option 2: Exit

**File Paths**
- empFile: data/MotorPH_Employee Data - Employee Details.csv
- attFile: data/MotorPH_Employee Data - Attendance Record.csv
- Falls back to project root if data folder not found

**Load Employee Details**
- Reads CSV line by line, parses quoted fields (e.g. address with commas)
- Stores empNum, lastName, firstName, birthday, hourlyRate in Map

**Load Attendance**
- Reads attendance CSV, stores empNum, date, logIn, logOut per record

**Compute Hours**
- Work hours 8 AM to 5 PM. 8:05 AM or earlier = on time (count from 8 AM)
- Logout after 5 PM counts until 5 PM only. 1 hour lunch deducted. Max 8 hours per day, no overtime

**Cutoffs**
- First cutoff: days 1-15
- Second cutoff: days 16 to end of month

**Deductions**
- SSS, PhilHealth, Pag-IBIG, Tax based on one month gross (1st + 2nd cutoff combined)
- Applied on second cutoff only. MotorPH matrices used.

**Display**
- First cutoff: Hours, Gross, Net
- Second cutoff: Hours, Gross, SSS, PhilHealth, Pag-IBIG, Tax, Total Deductions, Net
- June to December 2024. All employees sorted by employee number.

---

## Notes

- CSV files must be in the data folder
- Login: employee or payroll_staff, password 12345
- Works for year 2024
- Cutoffs: 1-15 and 16-end of month
