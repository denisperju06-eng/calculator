package calculator.core;

import calculator.errors.CalculatorException;

/**
 * Cerința (f): Transformarea hexazecimală și sisteme de numerație (HEX, DEC, OCT, BIN).
 */
public class BaseConverter {

    public String toHex(double decimalValue) {
        long intPart = (long) decimalValue;
        String hex = Long.toHexString(intPart).toUpperCase();
        
        double frac = Math.abs(decimalValue - intPart);
        if (frac > 1e-9) {
            StringBuilder sb = new StringBuilder(hex).append(".");
            double rem = frac;
            for (int i = 0; i < 4 && rem > 0; i++) {
                rem *= 16;
                int digit = (int) rem;
                sb.append(Integer.toHexString(digit).toUpperCase());
                rem -= digit;
            }
            return sb.toString();
        }
        return hex;
    }

    public String toOct(double decimalValue) {
        long intPart = (long) decimalValue;
        return Long.toOctalString(intPart);
    }

    public String toBin(double decimalValue) {
        long intPart = (long) decimalValue;
        String bin = Long.toBinaryString(intPart);
        
        // Formatare cu spațiu la fiecare 4 biți
        StringBuilder formatted = new StringBuilder();
        int len = bin.length();
        for (int i = 0; i < len; i++) {
            if (i > 0 && (len - i) % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(bin.charAt(i));
        }
        return formatted.toString();
    }

    public double parseToDecimal(String valueStr, String base) throws CalculatorException {
        String clean = valueStr.replaceAll("\\s+", "").trim();
        int radix;
        switch (base.toUpperCase()) {
            case "HEX":
            case "16":
                radix = 16;
                break;
            case "OCT":
            case "8":
                radix = 8;
                break;
            case "BIN":
            case "2":
                radix = 2;
                break;
            case "DEC":
            case "10":
                radix = 10;
                break;
            default:
                throw new CalculatorException("Bază de numerație nesuportată: " + base);
        }

        try {
            if (clean.contains(".")) {
                String[] parts = clean.split("\\.");
                long intPart = Long.parseLong(parts[0], radix);
                double fracPart = 0.0;
                for (int i = 0; i < parts[1].length(); i++) {
                    int digit = Character.digit(parts[1].charAt(i), radix);
                    if (digit < 0) throw new NumberFormatException();
                    fracPart += digit / Math.pow(radix, i + 1);
                }
                return (intPart >= 0 ? 1 : -1) * (Math.abs(intPart) + fracPart);
            } else {
                return Long.parseLong(clean, radix);
            }
        } catch (NumberFormatException e) {
            throw new CalculatorException("Valoarea '" + valueStr + "' nu este validă în baza " + base + "!");
        }
    }
}
