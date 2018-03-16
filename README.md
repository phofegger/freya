# Freya
Simple work shift planner and managment software for Android 4.0 and up. Optimized for tablets and bigger screens sizes. Could still use some optimizations and bug list is still pretty long (see TODO list). Data is handled via SQLite to keep the whole application very modularized.

## MainActivity.java
Main class. Responsible for the registering every employee, month and service as well as accessing the settings to change the behavior of application. Also used for reseting, backing up and importing.

## MonthEditActivity.java
Activity used to edit selected months. Contains quite a lot of layout calculations which needed to be optimized.

## Model
Contains all the definitions of data types used in the DB such as
- Contact
- Service
- ContactService (used in n:m table)
- Day
- Month

## Helper
Contains the helper class used throughout the lifecycle of the main activity
- DBHelper: Handles the communication with the database and the main application
- ViewPrintAdapter: Displays a specific month with all associated entries in PDF format which can be saved or printed

## Fragments
Costumarized fragments for months, services and contacts respectively
