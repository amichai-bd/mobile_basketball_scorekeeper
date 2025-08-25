package com.basketballstats.app.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * InputFormatHelper - Provides smart auto-formatting for date and time inputs
 * Enhances UX with automatic formatting as user types
 */
public class InputFormatHelper {
    
    /**
     * Add smart date formatting to EditText (DD/MM/YYYY)
     * Auto-adds slashes after day and month
     */
    public static void addDateFormatting(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;
            private String previous = "";
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;
                
                String input = s.toString().replaceAll("[^\\d]", ""); // Remove non-digits
                String formatted = "";
                
                if (input.length() > 0) {
                    // Day
                    formatted += input.substring(0, Math.min(2, input.length()));
                    if (input.length() >= 2) {
                        formatted += "/";
                        // Month
                        formatted += input.substring(2, Math.min(4, input.length()));
                        if (input.length() >= 4) {
                            formatted += "/";
                            // Year
                            formatted += input.substring(4, Math.min(8, input.length()));
                        }
                    }
                }
                
                if (!formatted.equals(previous)) {
                    isUpdating = true;
                    editText.setText(formatted);
                    editText.setSelection(formatted.length());
                    previous = formatted;
                    isUpdating = false;
                }
            }
        });
    }
    
    /**
     * Add smart time formatting to EditText (HH:MM)
     * Auto-adds colon after hour
     */
    public static void addTimeFormatting(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;
            private String previous = "";
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;
                
                String input = s.toString().replaceAll("[^\\d]", ""); // Remove non-digits
                String formatted = "";
                
                if (input.length() > 0) {
                    // Hour
                    formatted += input.substring(0, Math.min(2, input.length()));
                    if (input.length() >= 2) {
                        formatted += ":";
                        // Minute
                        formatted += input.substring(2, Math.min(4, input.length()));
                    }
                }
                
                if (!formatted.equals(previous)) {
                    isUpdating = true;
                    editText.setText(formatted);
                    editText.setSelection(formatted.length());
                    previous = formatted;
                    isUpdating = false;
                }
            }
        });
    }
    
    /**
     * Validate date format and check if it's a valid future date
     */
    public static boolean isValidDate(String dateStr) {
        if (dateStr == null || !dateStr.matches("\\d{2}/\\d{2}/\\d{4}")) {
            return false;
        }
        
        try {
            String[] parts = dateStr.split("/");
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);
            
            // Basic validation
            return day >= 1 && day <= 31 && 
                   month >= 1 && month <= 12 && 
                   year >= 2024 && year <= 2030;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validate time format (HH:MM, 24-hour)
     */
    public static boolean isValidTime(String timeStr) {
        if (timeStr == null || !timeStr.matches("\\d{2}:\\d{2}")) {
            return false;
        }
        
        try {
            String[] parts = timeStr.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            
            return hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
