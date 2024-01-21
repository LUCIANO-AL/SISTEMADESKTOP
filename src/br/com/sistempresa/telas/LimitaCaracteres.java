/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.sistempresa.telas;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *
 * @author Luciano & Paty
 */
public class LimitaCaracteres extends PlainDocument {

    public enum TipoEntrada {
        NUMEROINTEIRO, NUMERODECIMAL, NOME, EMAIL, DATA;
    }

    private int qtdCaracteres;
    private TipoEntrada tbEntrada;

    public LimitaCaracteres(int qtdCaracteres, TipoEntrada tbEntrada) {
        this.qtdCaracteres = qtdCaracteres;
        this.tbEntrada = tbEntrada;
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        if (str == null || getLength() == qtdCaracteres) {
            return;
        }

        int totalCarac = getLength() + str.length();

        String regex = "";

        switch (tbEntrada) {
            case NUMEROINTEIRO:
                regex = "[^0-9]";
                break;
            case NUMERODECIMAL:
                regex = "[^0-9,]";
                break;
            case NOME:
                regex = "[^\\p{IsLatin} ]";
                break;
            case EMAIL:
                regex = "[^\\p{IsLatin}@.\\-_][^0-9]";
                break;
            case DATA:
                regex = "[^0-9/]";
                break;
        }
        //Fazendo a substituição 
        str = str.replaceAll(regex, "");

        if (totalCarac <= qtdCaracteres) {
            super.insertString(offs, str, a); //To change body of generated methods, choose Tools | Templates.
        } else {
            String nova = str.substring(0, qtdCaracteres);
             super.insertString(offs, nova, a);
        }

    }

}
