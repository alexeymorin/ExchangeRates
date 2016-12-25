package com.github.alexeymorin.exchangerates;

class Valute {
    public final String numCode;
    public final String charCode;
    public final String nominal;
    public final String name;
    public final String value;

    public Valute(String numCode, String charCode, String nominal, String name, String value) {
        this.numCode = numCode;
        this.charCode = charCode;
        this.nominal = nominal;
        this.name = name;
        this.value = value;
    }
}

