package com.sistemajuridico.backend.core.domain.validators;

public final class DocumentoValidator {

    private DocumentoValidator() {
        // Construtor privado para classe utilitária
    }

    public static boolean isCpfCnpjValido(String documento) {
        if (documento == null) {
            return false;
        }

        String doc = documento.trim().toUpperCase();
        if (doc.isEmpty()) {
            return false;
        }

        if (doc.length() == 11) {
            return isValidCPF(doc);
        } else if (doc.length() == 14) {
            return isValidCNPJ(doc);
        }

        return false;
    }

    private static boolean isValidCPF(String cpf) {
        // CPF deve conter apenas dígitos numéricos
        for (int i = 0; i < 11; i++) {
            char c = cpf.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }

        // Rejeita sequências de dígitos iguais
        boolean allEqual = true;
        for (int i = 1; i < 11; i++) {
            if (cpf.charAt(i) != cpf.charAt(0)) {
                allEqual = false;
                break;
            }
        }
        if (allEqual) {
            return false;
        }

        // Primeiro dígito verificador
        int sum1 = 0;
        for (int i = 0; i < 9; i++) {
            sum1 += (cpf.charAt(i) - '0') * (10 - i);
        }
        int rest1 = 11 - (sum1 % 11);
        int digito1 = (rest1 >= 10) ? 0 : rest1;

        if (digito1 != (cpf.charAt(9) - '0')) {
            return false;
        }

        // Segundo dígito verificador
        int sum2 = 0;
        for (int i = 0; i < 10; i++) {
            sum2 += (cpf.charAt(i) - '0') * (11 - i);
        }
        int rest2 = 11 - (sum2 % 11);
        int digito2 = (rest2 >= 10) ? 0 : rest2;

        return digito2 == (cpf.charAt(10) - '0');
    }

    private static boolean isValidCNPJ(String cnpj) {
        // 12 primeiros caracteres podem ser alfanuméricos (0-9, A-Z)
        for (int i = 0; i < 12; i++) {
            char c = cnpj.charAt(i);
            boolean isDigit = (c >= '0' && c <= '9');
            boolean isLetter = (c >= 'A' && c <= 'Z');
            if (!isDigit && !isLetter) {
                return false;
            }
        }

        // 2 últimos caracteres (dígitos verificadores) devem ser estritamente numéricos
        for (int i = 12; i < 14; i++) {
            char c = cnpj.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }

        // Rejeita sequências com todos os caracteres iguais
        boolean allEqual = true;
        for (int i = 1; i < 14; i++) {
            if (cnpj.charAt(i) != cnpj.charAt(0)) {
                allEqual = false;
                break;
            }
        }
        if (allEqual) {
            return false;
        }

        // Pesos padrão para CNPJ
        int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int sum1 = 0;
        for (int i = 0; i < 12; i++) {
            int valorAscii = cnpj.charAt(i) - 48;
            sum1 += valorAscii * weights1[i];
        }
        int rest1 = sum1 % 11;
        int digito1 = (rest1 < 2) ? 0 : (11 - rest1);

        if (digito1 != (cnpj.charAt(12) - '0')) {
            return false;
        }

        int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int sum2 = 0;
        for (int i = 0; i < 13; i++) {
            int valorAscii = cnpj.charAt(i) - 48;
            sum2 += valorAscii * weights2[i];
        }
        int rest2 = sum2 % 11;
        int digito2 = (rest2 < 2) ? 0 : (11 - rest2);

        return digito2 == (cnpj.charAt(13) - '0');
    }
}

