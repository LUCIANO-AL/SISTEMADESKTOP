/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.sistempresa.telas;

/**
 *
 * @author Luciano & Paty
 */
import Atxy2k.CustomTextField.RestrictedTextField;
import java.sql.*;
import br.com.sistempresa.dal.ModuloConexao;
import com.mongodb.util.JSON;
import static java.awt.Frame.MAXIMIZED_BOTH;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.DefaultListModel;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;
import javax.swing.text.NumberFormatter;
import static mondrian.test.loader.DBLoader.Type.Decimal;

//a linha abaixo importa recursos da biblioteca sqlitejdbc-v056
import net.proteanit.sql.DbUtils;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.mapping.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class TelaSinistro extends javax.swing.JInternalFrame {

    //Usando a variavel de conexao do DAL
    Connection conexao = null;
    //Criando variaveis especiais para conexao com o banco 
    //Prepared Stetament e ResultSet são Frameworks do pacote java.sql
    // e servem para preparar e executar as instruções SQL
    PreparedStatement pst = null;
    PreparedStatement pst1 = null;
    ResultSet rs = null;
    PreparedStatement pst2 = null;
    ResultSet rs2 = null;

    DefaultListModel MODELOASSOC, MODELOFORN;

    private MaskFormatter valor, franquia, parcela;
    private MaskFormatter DataVencimento, DataEmissao;

    int Enter = 0;
    int EnterForn = 0;
    int EnterPesAssoc = 0;
    String idultimabaixa = "";
    String valorbaixaparcial = "";
    float valorbaixaparcialsonum;
    int totalouparcial;
    Double valormultajuros;
    int totaldiasatraso;
    int totalassociado;
    String idultimosinistro = "";
    int ultimoidassoc;
    String nomeultimoassoc = "";
    int diaParSin;
    int mesParSin;
    int anoParSin;
    LocalDate datavencimento;
    
    /**
     * Creates new form TelaCliente
     */
    public TelaSinistro() {

        initComponents();
        conexao = ModuloConexao.conector();

        try {

            DataVencimento = new MaskFormatter("##/##/####");
            DataEmissao = new MaskFormatter("##/##/####");
            valor = new MaskFormatter("###.###,##");
            franquia = new MaskFormatter("###.###,###");
            parcela = new MaskFormatter("###.###,##");

        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        java.util.Date data = new java.util.Date();
        DateFormat formatador = DateFormat.getDateInstance(DateFormat.MEDIUM);
        txtDataEmissao.setText(formatador.format(data));

        txtVenc.setFormatterFactory(new DefaultFormatterFactory(DataVencimento));

        // txtDataBaixaRec.setDocument(new LimitaCaracteres(60, LimitaCaracteres.TipoEntrada.DATA));
        txtIdSinistro.setDocument(new LimitaCaracteres(60, LimitaCaracteres.TipoEntrada.NUMEROINTEIRO));
        txtIdOrcamento.setDocument(new LimitaCaracteres(60, LimitaCaracteres.TipoEntrada.NUMEROINTEIRO));
        txtNF.setDocument(new LimitaCaracteres(60, LimitaCaracteres.TipoEntrada.NUMEROINTEIRO));
        //txtValoFranquia.setDocument(new LimitaCaracteres(60, LimitaCaracteres.TipoEntrada.NUMERODECIMAL));
        //txtValor.setDocument(new LimitaCaracteres(60, LimitaCaracteres.TipoEntrada.NUMERODECIMAL));
        txtValoParc.setDocument(new LimitaCaracteres(60, LimitaCaracteres.TipoEntrada.NUMERODECIMAL));
        
        DecimalFormat decimal = new DecimalFormat("###,###,###.00");
        NumberFormatter numFormatter = new NumberFormatter(decimal);
        numFormatter.setFormat(decimal);
        numFormatter.setAllowsInvalid(false);
        DefaultFormatterFactory dfFactory = new DefaultFormatterFactory(numFormatter);

        txtValoFranquia.setFormatterFactory(dfFactory);
        txtValor.setFormatterFactory(dfFactory);
        //txtValoParc.setFormatterFactory(dfFactory);

        //txtValor.setFormatterFactory(new DefaultFormatterFactory(valor));
        //txtValoFranquia.setFormatterFactory(new DefaultFormatterFactory(franquia));
        //txtValoParc.setFormatterFactory(new DefaultFormatterFactory(parcela));
        setarIdEmpresa();

        tornaInvisivel();

        mostrarPesquisaAssociado();
        mostrarPesquisaFornecedor();

        MODELOASSOC = new DefaultListModel();
        listAssociado.setModel(MODELOASSOC);

        MODELOFORN = new DefaultListModel();
        listFornecedor.setModel(MODELOFORN);

        popularComboBoxNomeBanco();
        setarTotalDeAssociados();

        txtDescricaoDanos.setLineWrap(true);
        txtDescricaoDanos.setWrapStyleWord(true);

        txtDescricaoConcerto.setLineWrap(true);
        txtDescricaoConcerto.setWrapStyleWord(true);
        

        //painelBaixa.setVisible(false);
        //stringdata = new SimpleDateFormat(txtVencRec.getText()).parse(data);
        /*java.util.Date data = new java.util.Date();
        DateFormat formatador = DateFormat.getDateInstance(DateFormat.SHORT);
        txtVencRec.setText(formatador.format(data));*/
    }

    private void adicionar() {

        String dia = txtVenc.getText().substring(0, 2);
        String mes = txtVenc.getText().substring(3, 5);
        String ano = txtVenc.getText().substring(6);

        String datamysql = ano + "-" + mes + "-" + dia;

        //java.util.Date dataEx = new java.util.Date();
        /*SimpleDateFormat formatar = new SimpleDateFormat("yyyy-MM-dd");
        String dataS = formatar.format(txtVencRec.getText());
        System.out.println(dataS);
        
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy"); 
        LocalDate data = LocalDate.parse(txtVencRec.getText(), formato); 
        System.out.println(data);*/
        String sql = "insert into tbsinistro(numorcamento, nf, associado, fornecedor, descrdano, descrconcerto, marca, modelo, placa, ano, renavan, vencimento, valor, franquia, valorparcelas, formapag, banco, parcelas, idveic, idassoc, idempr) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, txtIdOrcamento.getText());
            pst.setString(2, txtNF.getText());
            pst.setString(3, txtNomeAssoc.getText());
            pst.setString(4, txtNomeFornecedor.getText());
            pst.setString(5, txtDescricaoDanos.getText());
            pst.setString(6, txtDescricaoConcerto.getText());
            pst.setString(7, txtMarcaVeic.getText());
            pst.setString(8, txtModeloVeic.getText());
            pst.setString(9, txtPlacaVeic.getText());
            pst.setString(10, txtAnoVeic.getText());
            pst.setString(11, txtRenavanVeic.getText());
            pst.setString(12, datamysql);
            pst.setString(13, txtValor.getText().replace(".", "").replace(",", "."));
            pst.setString(14, txtValoFranquia.getText().replace(".", "").replace(",", "."));
            pst.setString(15, txtValoParc.getText().replace(".", "").replace(",", "."));
            pst.setString(16, cboFormaDePag.getSelectedItem().toString());
            pst.setString(17, cboBanco.getSelectedItem().toString());
            pst.setString(18, cboParcelas.getSelectedItem().toString());
            pst.setString(19, lblIdVeiculo.getText());
            pst.setString(20, lblIdAssociado.getText());
            pst.setString(21, txtIdEmpresa.getText());

            //Validação dos campos obrigatorios
            if ((txtNomeAssoc.getText().isEmpty()) || (txtValor.getText().isEmpty()) || (txtVenc.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatorios.");

            } else {
                int adicionado = pst.executeUpdate();

                buscaUltimoIdSinistro();
                adicionarContasAReceberFranquia();
                buscarUltimoIdAssoc();
                buscaNomeAssociado();

                for (int parcelas = Integer.parseInt(cboParcelas.getSelectedItem().toString()); parcelas > 0; parcelas--) {

                    setarTotalDeAssociados();
                    buscarUltimoIdAssoc();
                    buscaNomeAssociado();

                    for (totalassociado = totalassociado; totalassociado > 0; totalassociado--) {

                        dividirSinistroParaAssociados();

                        ultimoidassoc--;
                        buscaNomeAssociado();

                    }

                    // somar 1 mês = 29 de fevereiro de 2016
                    datavencimento = datavencimento.plusMonths(1);

                    if (mesParSin > 12) {
                        mesParSin = 1;
                        // somar 1 ano = 28 de fevereiro de 2017
                        datavencimento = datavencimento.plusYears(1);
                    }

                    Date date = Date.valueOf(datavencimento);
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                    String data = format.format(date);

                    System.out.println(data);

                    txtVenc.setText(data);

                    //txtVenc.setText(dataS);

                    /*  mesParSin++;

                    if (mesParSin > 12) {
                        mesParSin = 1;
                        anoParSin++;
                    }

                    if (mesParSin == 2) {
                        if (diaParSin > 29) {
                            diaParSin = 29;
                        }
                    }

                    String datamysql2 = String.valueOf(diaParSin) + "0" + String.valueOf(mesParSin) + String.valueOf(anoParSin);

                    txtVenc.setText(datamysql2);

                    System.out.println("Dia " + diaParSin);
                    System.out.println("Mes " + mesParSin);
                    System.out.println("Ano " + anoParSin);

                    System.out.println("Data " + datamysql2);*/
                }

                //System.out.println(adicionado);
                if (adicionado > 0) {

                    JOptionPane.showMessageDialog(null, "Sinistro criado com sucesso");

                    btnAdicionar.setEnabled(false);
                    btnAlterar.setEnabled(true);
                    btnRemover.setEnabled(true);
                    btnPesquisar.setEnabled(true);
                    btnImprimir.setEnabled(true);

                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void adicionarContasAReceberFranquia() {

        String dia = txtVenc.getText().substring(0, 2);
        String mes = txtVenc.getText().substring(3, 5);
        String ano = txtVenc.getText().substring(6);

        String datamysql = ano + "-" + mes + "-" + dia;

        String descricaosinistro = "Franquia referente ao sinistro de nº " + idultimosinistro + " . " + "Dano: " + txtDescricaoDanos.getText();

        String sql = "insert into tbcontasreceber(nossonumero, descricao, associado, valorprinc, valorrestante, parcelas, pago, formapag, datavenc, banco, idsin, idveic, idassoc,idempr, nomeassocsinistro, contaexcluida) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, "00000");
            pst.setString(2, descricaosinistro);
            pst.setString(3, txtNomeAssoc.getText());
            pst.setString(4, txtValoFranquia.getText().replace(".", "").replace(",", "."));
            pst.setString(5, txtValoFranquia.getText().replace(".", "").replace(",", "."));
            pst.setString(6, "1");
            pst.setString(7, "0");
            pst.setString(8, cboFormaDePag.getSelectedItem().toString());
            pst.setString(9, datamysql);
            pst.setString(10, cboBanco.getSelectedItem().toString());
            pst.setString(11, lblIdSinistro.getText());
            pst.setString(12, lblIdVeiculo.getText());
            pst.setString(13, lblIdAssociado.getText());
            pst.setString(14, txtIdEmpresa.getText());
            pst.setString(15, "");
            pst.setString(16, lblStatusContaExcluida.getText());

            int adicionado = pst.executeUpdate();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void dividirSinistroParaAssociados() {

        /* diaParSin = Integer.parseInt(txtVenc.getText().substring(0, 2));
        mesParSin = Integer.parseInt(txtVenc.getText().substring(3, 5));
        anoParSin = Integer.parseInt(txtVenc.getText().substring(6));

        datavencimento = LocalDate.of(anoParSin, mesParSin, diaParSin);*/
        diaParSin = Integer.parseInt(txtVenc.getText().substring(0, 2));
        mesParSin = Integer.parseInt(txtVenc.getText().substring(3, 5));
        anoParSin = Integer.parseInt(txtVenc.getText().substring(6));

        datavencimento = LocalDate.of(anoParSin, mesParSin, diaParSin);

        String datamysql = String.valueOf(anoParSin) + "-" + String.valueOf(mesParSin) + "-" + String.valueOf(diaParSin);

        String descricao = "Valor referente ao sinistro de nº " + idultimosinistro + " referente ao associado " + txtNomeAssoc.getText();

        String sql = "insert into tbcontasreceber(nossonumero, descricao, associado, valorprinc, valorrestante, parcelas, pago, formapag, datavenc, banco, idsin, idveic, idassoc,idempr, nomeassocsinistro, contaexcluida) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, "0000");
            pst.setString(2, descricao);
            pst.setString(3, nomeultimoassoc);// nome dos outros associados 
            pst.setString(4, txtValoParc.getText().replace(".", "").replace(",", "."));
            pst.setString(5, txtValoParc.getText().replace(".", "").replace(",", "."));
            pst.setString(6, "1");
            pst.setString(7, "0");
            pst.setString(8, cboFormaDePag.getSelectedItem().toString());
            pst.setString(9, datamysql);
            pst.setString(10, cboBanco.getSelectedItem().toString());
            pst.setString(11, lblIdSinistro.getText());
            pst.setString(12, lblIdVeiculo.getText());
            pst.setString(13, String.valueOf(ultimoidassoc));// id dos outros associados 
            pst.setString(14, txtIdEmpresa.getText());
            pst.setString(15, txtNomeAssoc.getText());
            pst.setString(16, lblStatusContaExcluida.getText());

            int adicionado = pst.executeUpdate();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    //Metodo para setar os campos do formulario com o conteudo da tabela    
    private void consultar() {

        String idsinistro = "";
        String idveiculo = "";
        String idassociado = "";

        String id_sin = JOptionPane.showInputDialog("Informe o ID do Sinistro?");

        String sql = "select idsin, numorcamento, nf, associado, fornecdor, descrdano, descrconcerto, marca, modelo, placa, ano, renavan, date_format(datasin,'%d/%m/%Y'), date_format(vencimento,'%d/%m/%Y'),\n"
                + "               Concat(  \n"
                + "                          Replace  \n"
                + "                           (Replace  \n"
                + "                                 (Replace  \n"
                + "                                  (Format(valor, 3), '.', '|'), ',', '.'), '|', ',')) AS Total\n"
                + "              ,Concat(   \n"
                + "                              Replace  \n"
                + "                              (Replace \n"
                + "                                  (Replace  \n"
                + "                                  (Format(franquia, 3), '.', '|'), ',', ''), '|', ',')) as Restante,"
                + "             Concat(   \n"
                + "                              Replace  \n"
                + "                              (Replace \n"
                + "                                  (Replace  \n"
                + "                                  (Format(valorparcelas, 3), '.', '|'), ',', ''), '|', ',')) as Parc,\n"
                + "				formapag, banco, parcelas, idveic, idassoc, idempr from tbsinistro where idsin = ?" + id_sin;

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                txtIdOrcamento.setText(rs.getString(1));
                txtNF.setText(rs.getString(2));
                txtNomeAssoc.setText(rs.getString(3));
                txtNomeFornecedor.setText(rs.getString(4));
                txtDescricaoDanos.setText(rs.getString(5));
                txtDescricaoConcerto.setText(rs.getString(6));
                txtMarcaVeic.setText(rs.getString(7));
                txtModeloVeic.setText(rs.getString(8));
                txtPlacaVeic.setText(rs.getString(9));
                txtAnoVeic.setText(rs.getString(10));
                txtRenavanVeic.setText(rs.getString(11));
                txtDataEmissao.setText(rs.getString(12));
                txtVenc.setText(rs.getString(13));
                txtValor.setText(rs.getString(14));
                txtValoFranquia.setText(rs.getString(15));
                txtValoParc.setText(rs.getString(16));
                cboFormaDePag.setSelectedItem(rs.getString(17));
                cboBanco.setSelectedItem(rs.getString(18));
                cboParcelas.setSelectedItem(rs.getString(19));
                lblIdVeiculo.setText(rs.getString(20));
                lblIdSinistro.setText(rs.getString(21));
                lblIdAssociado.setText(rs.getString(22));
                txtIdEmpresa.setText(rs.getString(23));

                btnAdicionar.setEnabled(true);
                btnAlterar.setEnabled(true);
                btnRemover.setEnabled(true);
                btnPesquisar.setEnabled(true);
                btnImprimir.setEnabled(true);
                btnLimpar.setEnabled(false);

            } else {
                JOptionPane.showMessageDialog(null, "Associado não cadastrado.");
                limpar();

            }
        } catch (SQLSyntaxErrorException e) {
            JOptionPane.showMessageDialog(null, "ID Inválido");
            //System.out.println(e);
        } catch (Exception e2) {
            JOptionPane.showMessageDialog(null, e2);
        }
    }

    private void alterar() {

        String dia = txtVenc.getText().substring(0, 2);
        String mes = txtVenc.getText().substring(3, 5);
        String ano = txtVenc.getText().substring(6);

        String datamysql = ano + "-" + mes + "-" + dia;

        String diaEmi = txtDataEmissao.getText().substring(0, 2);
        String mesEmi = txtDataEmissao.getText().substring(3, 5);
        String anoEmi = txtDataEmissao.getText().substring(6);

        String dataEmimysql = anoEmi + "-" + mesEmi + "-" + diaEmi;

        String sql = "update tbsinistro set numorcamento=?, nf=?, associado=?, fornecdor=?, descrdano=?, descrconcerto=?, marca=?, modelo=?, placa=?, ano= ?, renavan = ?, vencimento = ?, valor = ?, franquia = ?, formapag = ?, banco = ?, parcelas = ?  where idsin = ?";

        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, txtIdOrcamento.getText());
            pst.setString(2, txtNF.getText());
            pst.setString(3, txtNomeAssoc.getText());
            pst.setString(4, txtNomeFornecedor.getText());
            pst.setString(5, txtDescricaoDanos.getText());
            pst.setString(6, txtDescricaoConcerto.getText());
            pst.setString(7, txtMarcaVeic.getText());
            pst.setString(8, txtModeloVeic.getText());
            pst.setString(9, txtPlacaVeic.getText());
            pst.setString(10, txtAnoVeic.getText());
            pst.setString(11, txtRenavanVeic.getText());
            pst.setString(12, dataEmimysql);
            pst.setString(13, datamysql);
            pst.setString(14, txtValor.getText().replace(".", "").replace(",", "."));
            pst.setString(15, txtValoFranquia.getText().replace(".", "").replace(",", "."));
            pst.setString(16, txtValoParc.getText().replace(".", "").replace(",", "."));
            pst.setString(17, cboFormaDePag.getSelectedItem().toString());
            pst.setString(18, cboBanco.getSelectedItem().toString());
            pst.setString(19, cboParcelas.getSelectedItem().toString());
            pst.setString(12, txtIdSinistro.getText());

            //Validação dos campos obrigatorios
            if ((txtNomeAssoc.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatorios.");

            } else {
                int adicionado = pst.executeUpdate();

                //System.out.println(adicionado);
                if (adicionado > 0) {

                    JOptionPane.showMessageDialog(null, "Dados do sinistro alterados com sucesso");

                    limpar();

                    btnAdicionar.setEnabled(false);
                    btnAlterar.setEnabled(true);
                    btnRemover.setEnabled(true);
                    btnPesquisar.setEnabled(true);
                    btnImprimir.setEnabled(true);

                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void remover() {
        int confirma = JOptionPane.showConfirmDialog(null, "Tem Certeza que deseja excluir este sinistro.", "Atenção", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {
            String sql = "update tbcontasreceber set contaexcluida = 1 where idrec = ?";

            try {
                pst = conexao.prepareStatement(sql);
                pst.setString(1, txtIdOrcamento.getText());
                int apagado = pst.executeUpdate();

                if (apagado > 0) {
                    JOptionPane.showMessageDialog(null, "Conta removida com sucesso");

                    limpar();

                    btnAdicionar.setEnabled(true);
                    btnPesquisar.setEnabled(true);
                    btnAlterar.setEnabled(false);
                    btnRemover.setEnabled(false);
                    btnImprimir.setEnabled(false);
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    public void limpar() {
        txtIdOrcamento.setText(null);
        txtNF.setText(null);
        txtDescricaoDanos.setText(null);
        txtNomeAssoc.setText(null);
        txtValor.setText(null);
        txtValoFranquia.setText(null);

        cboFormaDePag.setSelectedIndex(0);
        txtDataEmissao.setText(null);
        txtVenc.setText(null);
        cboBanco.setSelectedIndex(0);
        String idsinistro = "";
        String idveiculo = "";
        String idassociado = "";
        txtIdEmpresa.setText(null);

        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(false);
        btnRemover.setEnabled(false);

        btnLimpar.setEnabled(false);

    }

    public void setarIdEmpresa() {

        TelaPrincipal princ = new TelaPrincipal();

        String nomefantasia_empr = princ.getCboEmpresa().getSelectedItem().toString();

        String sql = "select * from tbempresa where nomefanteempr = " + "'" + nomefantasia_empr + "'";

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                txtIdEmpresa.setText(rs.getString(1));

            }
        } catch (Exception e2) {
            JOptionPane.showMessageDialog(null, e2);
        }

    }

    public void setarIdAssociado() {

        String sql = "select * from tbassociado where NOMEASSOC = " + "'" + txtNomeAssoc.getText() + "'";

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                lblIdAssociado.setText(rs.getString(1));

            }
        } catch (Exception e2) {
            JOptionPane.showMessageDialog(null, e2);
        }

    }

    public void setarIdFornecedor() {

        String sql = "select * from tbfornecedor where NOMEFORNECEDOR = " + "'" + txtNomeFornecedor.getText() + "'";

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                lblIdForn.setText(rs.getString(1));

            }
        } catch (Exception e2) {
            JOptionPane.showMessageDialog(null, e2);
        }

    }

    public void setarTotalDeAssociados() {

        String sql = "SELECT COUNT(*) FROM tbassociado where associadoexcluido = 0 ";

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {

                totalassociado = Integer.parseInt(rs.getString(1));

            }
        } catch (Exception e2) {
            JOptionPane.showMessageDialog(null, e2);
        }

    }

    private void imprimirdoc_adesao() {

        int confirma = JOptionPane.showConfirmDialog(null, "Imprimir documento de adesão?", "Atenção", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {
            //Imprimir relatorio com o framework JasperReport
            try {
                // usando a classe HashMap para criar um filtro
                HashMap filtro = new HashMap();
                filtro.put("idassoc", Integer.parseInt(txtIdOrcamento.getText()));
                JasperPrint print = JasperFillManager.fillReport(getClass().getResourceAsStream("/impressao/termodeadesao.jasper"), filtro, conexao);
                //a linha abaixo apresenta o relatorio através da classe JasperViewer
                JasperViewer.viewReport(print, false);

            } catch (JRException e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    private void buscaUltimoIdSinistro() {

        String sql = "select idsin from tbsinistro order by idsin desc limit 1;";

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                idultimosinistro = rs.getString(1);

            }
        } catch (Exception e2) {
            JOptionPane.showMessageDialog(null, e2);
        }
    }

    public void listaDePesquisaAssociado() {

        String sql = "select * from tbassociado where associadoexcluido = 0 and NOMEASSOC like '" + txtNomeAssoc.getText() + "%' ORDER BY NOMEASSOC";

        try {
            pst = conexao.prepareStatement(sql);
            MODELOASSOC.removeAllElements();
            int v = 0;

            rs = pst.executeQuery();
            while (rs.next() & v < 4) {
                MODELOASSOC.addElement(rs.getString(3));
                v++;
            }

            if (v >= 1) {
                listAssociado.setVisible(true);
            } else {
                listAssociado.setVisible(false);
            }

            //txtNomeAssoc.setText(rs.getString("NOMEASSOC"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao listar dados" + e);
        }
    }

    public void listaDeForn() {

        String sql = "select * from tbfornecedor where NOMEFORNECEDOR like '" + txtNomeFornecedor.getText() + "%' ORDER BY NOMEFORNECEDOR";

        try {
            pst1 = conexao.prepareStatement(sql);
            MODELOFORN.removeAllElements();
            int v1 = 0;

            rs2 = pst1.executeQuery();
            while (rs2.next() & v1 < 4) {
                MODELOFORN.addElement(rs2.getString(3));
                v1++;
            }

            if (v1 >= 1) {
                listFornecedor.setVisible(true);
            } else {
                listFornecedor.setVisible(false);
            }

            //txtNomeAssoc.setText(rs.getString("NOMEASSOC"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao listar dados" + e);
        }
    }

    private void mostrarPesquisaAssociado() {

        String Linha = listAssociado.getSelectedValue();

        txtNomeAssoc.setText(Linha);

    }

    private void mostrarPesquisaFornecedor() {

        String Linha2 = listFornecedor.getSelectedValue();

        txtNomeFornecedor.setText(Linha2);

    }

    private void popularComboBoxNomeBanco() {

        String sql = "select * from tbbancos";

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            while (rs.next()) {

                cboBanco.addItem(rs.getString("nomebanco"));

            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void listaDeConjuntosAssoc() {

        String sql = "select idveic as ID, marca as Marca,  modelo as Modelo, placa as Placa, ano as Ano, renavan as RENAVAN from tbveiculo where idassoc = ?";

        try {
            pst = conexao.prepareStatement(sql);
            //Passando o conteudo da caixa de pesquisa para o ?
            //atenção ao "%" - continuação da pesquisa sql
            pst.setString(1, lblIdAssociado.getText());
            rs = pst.executeQuery();
            // a linha abaixo usa a biblioteca rs2xml.jar para preencher a tabela 
            tblVeiculosAssoc.setModel(DbUtils.resultSetToTableModel(rs));

            tblVeiculosAssoc.getColumnModel().getColumn(0).setMaxWidth(50);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void tornaInvisivel() {
        lblIdSinistro.setVisible(false);
        lblIdVeiculo.setVisible(false);
        lblIdAssociado.setVisible(false);
        txtIdEmpresa.setVisible(false);
        lblStatusContaExcluida.setVisible(false);
        listAssociado.setVisible(false);
        listFornecedor.setVisible(false);

    }

    public void setar_veiculo() {

        int setar = tblVeiculosAssoc.getSelectedRow();

        lblIdVeiculo.setText(tblVeiculosAssoc.getModel().getValueAt(setar, 0).toString());

        String id_veic = lblIdVeiculo.getText();

        String sql = "select idveic, marca, modelo, ano, placa, renavan from tbveiculo where idveic = " + id_veic;

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {

                lblIdVeiculo.setText(rs.getString(1));
                txtMarcaVeic.setText(rs.getString(2));
                txtModeloVeic.setText(rs.getString(3));
                txtAnoVeic.setText(rs.getString(4));
                txtPlacaVeic.setText(rs.getString(5));
                txtRenavanVeic.setText(rs.getString(6));

                /*  btnAdicionar.setEnabled(true);
                btnAlterar.setEnabled(true);
                btnRemover.setEnabled(true);
                btnPesquisar.setEnabled(true);
                btnImprimir.setEnabled(true);*/
            } else {
                JOptionPane.showMessageDialog(null, "Associado não cadastrado.");
                limpar();

            }
        } catch (SQLSyntaxErrorException e) {
            JOptionPane.showMessageDialog(null, "ID Inválido");
            //System.out.println(e);
        } catch (Exception e2) {
            JOptionPane.showMessageDialog(null, e2);
        }
        //A linha abaixo desabilita o botão adicionar
        //btnAdicionar.setEnabled(false);
    }

    private void buscaNomeAssociado() {

        String sql = "select NOMEASSOC from tbassociado where IDASSOC = ?";

        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, String.valueOf(ultimoidassoc));
            rs = pst.executeQuery();
            if (rs.next()) {
                nomeultimoassoc = rs.getString(1);
            }
        } catch (Exception e2) {
            JOptionPane.showMessageDialog(null, e2);
        }
    }

    private void buscarUltimoIdAssoc() {

        String sql = "select IDASSOC from tbassociado order by IDASSOC desc limit 1;";

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                ultimoidassoc = Integer.parseInt(rs.getString(1));

            }
        } catch (Exception e2) {
            JOptionPane.showMessageDialog(null, e2);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtNomeAssoc = new javax.swing.JTextField();
        listAssociado = new javax.swing.JList<>();
        btnAdicionar = new javax.swing.JButton();
        btnAlterar = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        btnRemover = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        btnImprimir = new javax.swing.JButton();
        lblCnpjCpf = new javax.swing.JLabel();
        btnPesquisar = new javax.swing.JButton();
        btnLimpar = new javax.swing.JButton();
        jLabel19 = new javax.swing.JLabel();
        txtIdEmpresa = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        txtValoFranquia = new javax.swing.JFormattedTextField();
        jLabel22 = new javax.swing.JLabel();
        cboFormaDePag = new javax.swing.JComboBox<>();
        txtNF = new javax.swing.JTextField();
        txtDataEmissao = new javax.swing.JFormattedTextField();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        txtVenc = new javax.swing.JFormattedTextField();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        cboBanco = new javax.swing.JComboBox<>();
        lblIdSinistro = new javax.swing.JLabel();
        lblIdVeiculo = new javax.swing.JLabel();
        lblIdAssociado = new javax.swing.JLabel();
        lblStatusContaExcluida = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblVeiculosAssoc = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        txtValor = new javax.swing.JFormattedTextField();
        jLabel14 = new javax.swing.JLabel();
        txtNomeFornecedor = new javax.swing.JTextField();
        listFornecedor = new javax.swing.JList<>();
        jLabel18 = new javax.swing.JLabel();
        txtPlacaVeic = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtMarcaVeic = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        txtRenavanVeic = new javax.swing.JFormattedTextField();
        txtAnoVeic = new javax.swing.JFormattedTextField();
        jLabel16 = new javax.swing.JLabel();
        txtModeloVeic = new javax.swing.JTextField();
        txtIdSinistro = new javax.swing.JTextField();
        cboParcelas = new javax.swing.JComboBox<>();
        jLabel8sin = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        txtValoParc = new javax.swing.JFormattedTextField();
        jLabel28 = new javax.swing.JLabel();
        lblIdForn = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtDescricaoDanos = new javax.swing.JTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtDescricaoConcerto = new javax.swing.JTextArea();
        txtIdOrcamento = new javax.swing.JTextField();

        jLabel2.setText("jLabel2");

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setTitle("Sinistro");
        setToolTipText("");
        setPreferredSize(new java.awt.Dimension(900, 719));

        jLabel1.setText("Associado");

        txtNomeAssoc.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtNomeAssocFocusLost(evt);
            }
        });
        txtNomeAssoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNomeAssocActionPerformed(evt);
            }
        });
        txtNomeAssoc.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtNomeAssocKeyReleased(evt);
            }
        });

        listAssociado.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        listAssociado.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                listAssociadoMousePressed(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listAssociadoMouseClicked(evt);
            }
        });

        btnAdicionar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/sistempresa/icones/create.png"))); // NOI18N
        btnAdicionar.setToolTipText("Adicionar Usuário");
        btnAdicionar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAdicionar.setPreferredSize(new java.awt.Dimension(80, 80));
        btnAdicionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdicionarActionPerformed(evt);
            }
        });

        btnAlterar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/sistempresa/icones/update.png"))); // NOI18N
        btnAlterar.setToolTipText("Alterar");
        btnAlterar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAlterar.setEnabled(false);
        btnAlterar.setPreferredSize(new java.awt.Dimension(80, 80));
        btnAlterar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAlterarActionPerformed(evt);
            }
        });

        jLabel5.setText("* Campos obrigatórios");

        btnRemover.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/sistempresa/icones/delete.png"))); // NOI18N
        btnRemover.setToolTipText("Apagar");
        btnRemover.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnRemover.setEnabled(false);
        btnRemover.setPreferredSize(new java.awt.Dimension(80, 80));
        btnRemover.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoverActionPerformed(evt);
            }
        });

        jLabel7.setText("Nº Orçamento");

        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/sistempresa/icones/imprimir.png"))); // NOI18N
        btnImprimir.setToolTipText("Imprimir Doc de Adesão");
        btnImprimir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnImprimir.setEnabled(false);
        btnImprimir.setPreferredSize(new java.awt.Dimension(80, 80));
        btnImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImprimirActionPerformed(evt);
            }
        });

        lblCnpjCpf.setText("NF");

        btnPesquisar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/sistempresa/icones/read.png"))); // NOI18N
        btnPesquisar.setToolTipText("Consultar");
        btnPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnPesquisar.setPreferredSize(new java.awt.Dimension(80, 80));
        btnPesquisar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPesquisarActionPerformed(evt);
            }
        });

        btnLimpar.setText("Limpar Campos");
        btnLimpar.setEnabled(false);
        btnLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimparActionPerformed(evt);
            }
        });

        jLabel19.setText("Data");

        jLabel20.setText("Valor");

        jLabel21.setText("Franquia");

        txtValoFranquia.setText("0.00");
        txtValoFranquia.setToolTipText("");
        txtValoFranquia.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtValoFranquiaFocusLost(evt);
            }
        });
        txtValoFranquia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtValoFranquiaActionPerformed(evt);
            }
        });

        jLabel22.setText("Forma de Pag.");

        cboFormaDePag.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Boleto", "Cartão", "Dinheiro" }));

        txtNF.setText("00");
        txtNF.setToolTipText("");

        txtDataEmissao.setEnabled(false);

        jLabel23.setText("Descrição dos Danos");

        jLabel24.setText("Vencimento");

        txtVenc.setToolTipText("");
        txtVenc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtVencActionPerformed(evt);
            }
        });

        jLabel25.setText("Parc.");

        jLabel26.setText("Banco");

        lblIdSinistro.setText("0");

        lblIdVeiculo.setText("0");

        lblIdAssociado.setText("0");

        lblStatusContaExcluida.setText("0");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Conjuntos"));

        tblVeiculosAssoc = new javax.swing.JTable(){
            public boolean isCellEditable(int rowIndex, int colIndex){
                return false;
            }
        };
        tblVeiculosAssoc.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Id", "Marca", "Modelo", "Placa", "Ano", "RENAVAN"
            }
        ));
        tblVeiculosAssoc.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblVeiculosAssocMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblVeiculosAssoc);

        jLabel4.setText("Clique no veículo que sofreu o sinistro para preencher os campos acima. ");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 696, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel4)
                .addGap(43, 43, 43))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        txtValor.setText("0");
        txtValor.setToolTipText("");
        txtValor.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                txtValorCaretPositionChanged(evt);
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                txtValorInputMethodTextChanged(evt);
            }
        });
        txtValor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtValorActionPerformed(evt);
            }
        });

        jLabel14.setText("Fornecedor");

        txtNomeFornecedor.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtNomeFornecedorFocusLost(evt);
            }
        });
        txtNomeFornecedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNomeFornecedorActionPerformed(evt);
            }
        });
        txtNomeFornecedor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtNomeFornecedorKeyReleased(evt);
            }
        });

        listFornecedor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        listFornecedor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                listFornecedorMousePressed(evt);
            }
        });

        jLabel18.setText("Placa");

        txtPlacaVeic.setEnabled(false);
        txtPlacaVeic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPlacaVeicActionPerformed(evt);
            }
        });

        jLabel3.setText("Marca");

        txtMarcaVeic.setEnabled(false);

        jLabel29.setText("RENAVAN");

        jLabel30.setText("Ano");

        txtRenavanVeic.setToolTipText("");
        txtRenavanVeic.setEnabled(false);
        txtRenavanVeic.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtRenavanVeicFocusLost(evt);
            }
        });

        txtAnoVeic.setEnabled(false);
        txtAnoVeic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAnoVeicActionPerformed(evt);
            }
        });

        jLabel16.setText("Modelo");

        txtModeloVeic.setEnabled(false);

        txtIdSinistro.setEnabled(false);
        txtIdSinistro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdSinistroActionPerformed(evt);
            }
        });

        cboParcelas.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "01", "02", "03" }));
        cboParcelas.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboParcelasItemStateChanged(evt);
            }
        });

        jLabel8sin.setText("Nº do Sinistro");

        jLabel27.setText("Descrição do Concerto");

        txtValoParc.setText("0.00");
        txtValoParc.setToolTipText("");
        txtValoParc.setEnabled(false);
        txtValoParc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtValoParcActionPerformed(evt);
            }
        });

        jLabel28.setText("Valor Parc.");

        lblIdForn.setText("id forn");

        txtDescricaoDanos.setColumns(20);
        txtDescricaoDanos.setRows(5);
        jScrollPane3.setViewportView(txtDescricaoDanos);

        txtDescricaoConcerto.setColumns(20);
        txtDescricaoConcerto.setRows(5);
        jScrollPane1.setViewportView(txtDescricaoConcerto);

        txtIdOrcamento.setText("00");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblIdVeiculo)
                    .addComponent(lblStatusContaExcluida)
                    .addComponent(txtIdEmpresa, javax.swing.GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
                    .addComponent(lblIdAssociado, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblIdSinistro, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(61, 61, 61)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8sin, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel23, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel27, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtIdSinistro, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtIdOrcamento, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(lblCnpjCpf)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtNF, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel19)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtDataEmissao, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txtNomeFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, 609, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtNomeAssoc, javax.swing.GroupLayout.PREFERRED_SIZE, 608, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(listFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, 609, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(listAssociado, javax.swing.GroupLayout.PREFERRED_SIZE, 609, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 609, Short.MAX_VALUE)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(145, 145, 145)
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtValoFranquia, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel25)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cboParcelas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel28)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtValoParc, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(139, 139, 139)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(2, 2, 2)
                                .addComponent(jLabel18)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtPlacaVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(34, 34, 34)
                                .addComponent(jLabel30)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtAnoVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel29)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtRenavanVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtMarcaVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel16)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtModeloVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 425, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(10, 10, 10)))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblIdForn)
                        .addGap(93, 93, 93)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel22)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cboFormaDePag, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel26)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cboBanco, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel24)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtVenc, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(btnPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(btnRemover, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(btnImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(39, 39, 39)
                                    .addComponent(btnLimpar)
                                    .addGap(48, 48, 48)))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel5)))
                .addContainerGap(83, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jLabel5)
                .addGap(13, 13, 13)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(txtDataEmissao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCnpjCpf)
                    .addComponent(jLabel7)
                    .addComponent(txtIdSinistro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8sin)
                    .addComponent(txtIdOrcamento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(txtNomeFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addComponent(listFornecedor)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtNomeAssoc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addComponent(listAssociado)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(jLabel23)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(98, 98, 98)
                        .addComponent(lblIdSinistro)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblIdVeiculo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblIdAssociado)
                        .addGap(6, 6, 6)
                        .addComponent(txtIdEmpresa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblStatusContaExcluida)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblIdForn)
                        .addGap(142, 142, 142))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addComponent(jLabel27)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel20)
                            .addComponent(jLabel21)
                            .addComponent(txtValoFranquia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel25)
                            .addComponent(cboParcelas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel28)
                            .addComponent(txtValoParc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel22)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel26)
                                .addComponent(cboBanco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cboFormaDePag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtVenc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel24)))
                        .addGap(3, 3, 3)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel16)
                                .addComponent(jLabel3)
                                .addComponent(txtMarcaVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txtModeloVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel18)
                            .addComponent(txtPlacaVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel30)
                            .addComponent(txtAnoVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel29)
                            .addComponent(txtRenavanVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(23, 23, 23)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnRemover, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnLimpar)))
                        .addGap(49, 49, 49))))
        );

        btnImprimir.getAccessibleContext().setAccessibleDescription("Imprimir Documento de Adesão");
        btnPesquisar.getAccessibleContext().setAccessibleDescription("Pesquisar Por ID");

        setBounds(0, 0, 951, 788);
    }// </editor-fold>//GEN-END:initComponents

    private void txtNomeAssocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNomeAssocActionPerformed
        listAssociado.setVisible(false);
        Enter = 1;
    }//GEN-LAST:event_txtNomeAssocActionPerformed

    private void btnAdicionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarActionPerformed
        adicionar();
    }//GEN-LAST:event_btnAdicionarActionPerformed

    private void btnAlterarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlterarActionPerformed
        alterar();
    }//GEN-LAST:event_btnAlterarActionPerformed

    private void btnRemoverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoverActionPerformed
        remover();
    }//GEN-LAST:event_btnRemoverActionPerformed

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        imprimirdoc_adesao();
    }//GEN-LAST:event_btnImprimirActionPerformed

    private void btnPesquisarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPesquisarActionPerformed
        consultar();
    }//GEN-LAST:event_btnPesquisarActionPerformed

    private void btnLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparActionPerformed
        limpar();
        btnImprimir.setEnabled(false);
    }//GEN-LAST:event_btnLimparActionPerformed

    private void txtValoFranquiaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtValoFranquiaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtValoFranquiaActionPerformed

    private void txtVencActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtVencActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtVencActionPerformed

    private void txtNomeAssocKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNomeAssocKeyReleased
        if (Enter == 0) {
            listaDePesquisaAssociado();
        } else {
            Enter = 0;
        }

    }//GEN-LAST:event_txtNomeAssocKeyReleased

    private void listAssociadoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listAssociadoMouseClicked

    }//GEN-LAST:event_listAssociadoMouseClicked

    private void txtValorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtValorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtValorActionPerformed

    private void txtNomeAssocFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNomeAssocFocusLost
        listAssociado.setVisible(false);
        Enter = 0;
    }//GEN-LAST:event_txtNomeAssocFocusLost

    private void txtNomeFornecedorFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNomeFornecedorFocusLost
        listFornecedor.setVisible(false);
        EnterForn = 0;
    }//GEN-LAST:event_txtNomeFornecedorFocusLost

    private void txtNomeFornecedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNomeFornecedorActionPerformed
        listFornecedor.setVisible(false);
        EnterForn = 1;
    }//GEN-LAST:event_txtNomeFornecedorActionPerformed

    private void txtNomeFornecedorKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNomeFornecedorKeyReleased
        if (EnterForn == 0) {
            listaDeForn();
        } else {
            EnterForn = 0;
        }
    }//GEN-LAST:event_txtNomeFornecedorKeyReleased

    private void listFornecedorMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listFornecedorMousePressed
        mostrarPesquisaFornecedor();
        listFornecedor.setVisible(false);
        setarIdFornecedor();
    }//GEN-LAST:event_listFornecedorMousePressed

    private void txtPlacaVeicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPlacaVeicActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPlacaVeicActionPerformed

    private void txtRenavanVeicFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtRenavanVeicFocusLost

    }//GEN-LAST:event_txtRenavanVeicFocusLost

    private void txtAnoVeicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAnoVeicActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAnoVeicActionPerformed

    private void txtIdSinistroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdSinistroActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIdSinistroActionPerformed

    private void txtValoParcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtValoParcActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtValoParcActionPerformed

    private void cboParcelasItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboParcelasItemStateChanged

        Double valor = Double.parseDouble(txtValor.getText().replace(".", "").replace(",", "."));
        Double parcelas = (valor / totalassociado);
        int numparc = Integer.parseInt(cboParcelas.getSelectedItem().toString());
        Double parcparcelasdas = parcelas / numparc;

        txtValoParc.setText(String.valueOf(new DecimalFormat("#,##0.00").format(parcparcelasdas)));
        
       // System.out.println(String.valueOf(new DecimalFormat("#,##0.00").format(parcparcelasdas)));
        
        
    }//GEN-LAST:event_cboParcelasItemStateChanged

    private void listAssociadoMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listAssociadoMousePressed
        mostrarPesquisaAssociado();
        listAssociado.setVisible(false);
        setarIdAssociado();
        listaDeConjuntosAssoc();
    }//GEN-LAST:event_listAssociadoMousePressed

    private void tblVeiculosAssocMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblVeiculosAssocMouseClicked
        setar_veiculo();
    }//GEN-LAST:event_tblVeiculosAssocMouseClicked

    private void txtValoFranquiaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtValoFranquiaFocusLost
              
       Double valor = Double.parseDouble(txtValor.getText().replace(".", "").replace(",", "."));
        Double parcelas = (valor / totalassociado);
        int numparc = Integer.parseInt(cboParcelas.getSelectedItem().toString());
        Double parcparcelasdas = parcelas / numparc;

        txtValoParc.setText(String.valueOf(new DecimalFormat("#,##0.00").format(parcparcelasdas)));
        
       // System.out.println(String.valueOf(new DecimalFormat("#,##0.00").format(parcparcelasdas)));
        
    }//GEN-LAST:event_txtValoFranquiaFocusLost

    private void txtValorInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_txtValorInputMethodTextChanged

    }//GEN-LAST:event_txtValorInputMethodTextChanged

    private void txtValorCaretPositionChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_txtValorCaretPositionChanged

    }//GEN-LAST:event_txtValorCaretPositionChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionar;
    private javax.swing.JButton btnAlterar;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnLimpar;
    private javax.swing.JButton btnPesquisar;
    private javax.swing.JButton btnRemover;
    public static javax.swing.JComboBox<String> cboBanco;
    public static javax.swing.JComboBox<String> cboFormaDePag;
    public static javax.swing.JComboBox<String> cboParcelas;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8sin;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblCnpjCpf;
    private javax.swing.JLabel lblIdAssociado;
    private javax.swing.JLabel lblIdForn;
    private javax.swing.JLabel lblIdSinistro;
    private javax.swing.JLabel lblIdVeiculo;
    private javax.swing.JLabel lblStatusContaExcluida;
    private javax.swing.JList<String> listAssociado;
    private javax.swing.JList<String> listFornecedor;
    private javax.swing.JTable tblVeiculosAssoc;
    private javax.swing.JFormattedTextField txtAnoVeic;
    private javax.swing.JFormattedTextField txtDataEmissao;
    private javax.swing.JTextArea txtDescricaoConcerto;
    private javax.swing.JTextArea txtDescricaoDanos;
    private javax.swing.JTextField txtIdEmpresa;
    private javax.swing.JTextField txtIdOrcamento;
    public static javax.swing.JTextField txtIdSinistro;
    private javax.swing.JTextField txtMarcaVeic;
    private javax.swing.JTextField txtModeloVeic;
    private javax.swing.JTextField txtNF;
    public static javax.swing.JTextField txtNomeAssoc;
    public static javax.swing.JTextField txtNomeFornecedor;
    private javax.swing.JTextField txtPlacaVeic;
    private javax.swing.JFormattedTextField txtRenavanVeic;
    private javax.swing.JFormattedTextField txtValoFranquia;
    private javax.swing.JFormattedTextField txtValoParc;
    private javax.swing.JFormattedTextField txtValor;
    private javax.swing.JFormattedTextField txtVenc;
    // End of variables declaration//GEN-END:variables
}
