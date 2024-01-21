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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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

public class TelaContasReceber extends javax.swing.JInternalFrame {

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

    DefaultListModel MODELOASSOC, MODASSOCSIN, MODELOPESQASSOC;

    private MaskFormatter ValorPrincipal, ValorRestante;
    private MaskFormatter DataVencimento, DataEmissao;

    int Enter = 0;
    int EnterSin = 0;
    int EnterPesAssoc = 0;
    String idultimabaixa = "";
    String valorbaixaparcial = "";
    float valorbaixaparcialsonum;
    int totalouparcial;
    Double valormultajuros;
    int totaldiasatraso;
    String datamaisantiga;
    LocalDate localdatavencimento;
    int maiorparcela;

    /**
     * Creates new form TelaCliente
     */
    public TelaContasReceber() {

        initComponents();
        conexao = ModuloConexao.conector();

        try {

            DataVencimento = new MaskFormatter("##/##/####");
            DataEmissao = new MaskFormatter("##/##/####");

        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        java.util.Date data = new java.util.Date();
        DateFormat formatador = DateFormat.getDateInstance(DateFormat.MEDIUM);
        txtDataBaixaRec.setText(formatador.format(data));

        txtVencRec.setFormatterFactory(new DefaultFormatterFactory(DataVencimento));
        // txtDataBaixaRec.setDocument(new LimitaCaracteres(60, LimitaCaracteres.TipoEntrada.DATA));

        //txtValorRest.setDocument(new LimitaCaracteres(60, LimitaCaracteres.TipoEntrada.NUMERODECIMAL));
        //txtValorPrinc.setDocument(new LimitaCaracteres(60, LimitaCaracteres.TipoEntrada.NUMERODECIMAL));
        txtMultaRec.setDocument(new LimitaCaracteres(60, LimitaCaracteres.TipoEntrada.NUMERODECIMAL));
        txtJurosRec.setDocument(new LimitaCaracteres(60, LimitaCaracteres.TipoEntrada.NUMERODECIMAL));
        
        DecimalFormat decimal = new DecimalFormat("###,###,###.00");
        NumberFormatter numFormatter = new NumberFormatter(decimal);
        numFormatter.setFormat(decimal);
        numFormatter.setAllowsInvalid(false);
        DefaultFormatterFactory dfFactory = new DefaultFormatterFactory(numFormatter);

        txtValorPrinc.setFormatterFactory(dfFactory);
        txtValorRest.setFormatterFactory(dfFactory);

        setarIdEmpresa();

        mostrarPesquisaAssociado();
        mostrarPesquisaAssociadoSinistro();
        mostrarAssociadoCampoPesquisa();

        tornaInvisivel();

        MODELOASSOC = new DefaultListModel();
        MODASSOCSIN = new DefaultListModel();
        MODELOPESQASSOC = new DefaultListModel();

        listAssociado.setModel(MODELOASSOC);
        listAssocSinistro.setModel(MODASSOCSIN);
        listPesquisarAssociaodo.setModel(MODELOPESQASSOC);

        popularComboBoxNomeBanco();
        setarJurosBanco();
        setarMultaBanco();

        //painelBaixa.setVisible(false);
        //stringdata = new SimpleDateFormat(txtVencRec.getText()).parse(data);
        /*java.util.Date data = new java.util.Date();
        DateFormat formatador = DateFormat.getDateInstance(DateFormat.SHORT);
        txtVencRec.setText(formatador.format(data));*/
    }

    private void adicionar() {

        String status = labelStatusRec.getText();
        int statusnum = 0;

        String dia = txtVencRec.getText().substring(0, 2);
        String mes = txtVencRec.getText().substring(3, 5);
        String ano = txtVencRec.getText().substring(6);

        String datamysql = ano + "-" + mes + "-" + dia;

        if (status == "Pago") {
            statusnum = 1;
        }

        //java.util.Date dataEx = new java.util.Date();
        /*SimpleDateFormat formatar = new SimpleDateFormat("yyyy-MM-dd");
        String dataS = formatar.format(txtVencRec.getText());
        System.out.println(dataS);
        
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy"); 
        LocalDate data = LocalDate.parse(txtVencRec.getText(), formato); 
        System.out.println(data);*/
        String sql = "insert into tbcontasreceber(nossonumero, descricao, associado, valorprinc, valorrestante, parcelas, pago, formapag, datavenc, banco, idsin, idveic, idassoc,idempr, nomeassocsinistro, contaexcluida, mensalidade) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, txtNossoNum.getText());
            pst.setString(2, txtDescricaoRec.getText());
            pst.setString(3, txtNomeAssoc.getText());
            pst.setString(4, txtValorPrinc.getText().replace(".", "").replace(",", "."));
            pst.setString(5, txtValorRest.getText().replace(".", "").replace(",", "."));
            pst.setString(6, txtParc.getText());
            pst.setString(7, String.valueOf(statusnum));
            pst.setString(8, cboFormaDePag.getSelectedItem().toString());
            pst.setString(9, datamysql);
            //pst.setString(9, txtVencRec.getText());
            pst.setString(10, cboBanco.getSelectedItem().toString());
            pst.setString(11, lblIdSinistro.getText());
            pst.setString(12, lblIdVeiculo.getText());
            pst.setString(13, lblIdAssociado.getText());
            pst.setString(14, txtIdEmpresa.getText());
            pst.setString(15, txtNomeAssocSinistro.getText());
            pst.setString(16, lblStatusContaExcluida.getText());
            pst.setString(17, "0");

            //Validação dos campos obrigatorios
            if ((txtNomeAssoc.getText().isEmpty()) || (txtValorPrinc.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatorios.");

            } else {
                int adicionado = pst.executeUpdate();

                //System.out.println(adicionado);
                if (adicionado > 0) {

                    JOptionPane.showMessageDialog(null, "Conta a receber adicionado com sucesso");

                    btnAdicionar.setEnabled(false);
                    btnAlterar.setEnabled(true);
                    btnRemover.setEnabled(true);
                    btnPesquisar.setEnabled(true);
                    btnImprimir.setEnabled(true);

                    buscaUltimoId();

                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    //PEsquisa cliente pelo nome
    private void pesquisar_cliente() {

        String sql = "select idrec as ID, associado as Associado, date_format(dataemissao,'%d/%m/%Y') as Emissao, date_format(datavenc,'%d/%m/%Y') as Vencimento, valorprinc as Total, valorrestante AS Restante from tbcontasreceber where contaexcluida = 0 and associado like ?";

        try {
            pst = conexao.prepareStatement(sql);
            //Passando o conteudo da caixa de pesquisa para o ?
            //atenção ao "%" - continuação da pesquisa sql
            pst.setString(1, txtAssocPesquisar.getText() + "%");
            rs = pst.executeQuery();
            // a linha abaixo usa a biblioteca rs2xml.jar para preencher a tabela 
            tblAssociadosReceber.setModel(DbUtils.resultSetToTableModel(rs));

            tblAssociadosReceber.getColumnModel().getColumn(0).setMaxWidth(50);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    //Metodo para setar os campos do formulario com o conteudo da tabela    
    public void setar_campos() {

        int setar = tblAssociadosReceber.getSelectedRow();

        txtIdRec.setText(tblAssociadosReceber.getModel().getValueAt(setar, 0).toString());

        String id_rec = txtIdRec.getText();

        String sql = "select idrec, nossonumero, descricao, associado, Concat(   \n"
                + "               Replace  \n"
                + "                 (Replace  \n"
                + "                   (Replace  \n"
                + "                     (Format(valorprinc, 3), '.', '|'), ',', ''), '|', ',')) AS Total"
                + " ,Concat(   \n"
                + "               Replace  \n"
                + "                 (Replace  \n"
                + "                   (Replace  \n"
                + "                     (Format(valorrestante, 3), '.', '|'), ',', ''), '|', ',')) as Restante,"
                + " parcelas, pago, formapag, date_format(dataemissao,'%d/%m/%Y'), date_format(datavenc,'%d/%m/%Y'),banco, idsin, idveic, idassoc, idempr, nomeassocsinistro, mensalidade from tbcontasreceber where idrec =" + id_rec;

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                txtIdRec.setText(rs.getString(1));
                txtNossoNum.setText(rs.getString(2));
                txtDescricaoRec.setText(rs.getString(3));
                txtNomeAssoc.setText(rs.getString(4));
                txtValorPrinc.setText(rs.getString(5));
                txtValorRest.setText(rs.getString(6));

                if ("0,000".equals(txtValorRest.getText())) {
                    btnBaixcarConta.setEnabled(false);
                } else {
                    btnBaixcarConta.setEnabled(true);
                }
                txtValorBaixaRec.setText(rs.getString(6));
                txtParc.setText(rs.getString(7));
                labelStatusRec.setText(rs.getString(8));

                String status = labelStatusRec.getText();

                if ("0".equals(status)) {
                    labelStatusRec.setText("Aberto");
                } else {
                    labelStatusRec.setText("Pago");
                }
                cboFormaDePag.setSelectedItem(rs.getString(9));
                txtDataEmissao.setText(rs.getString(10));
                txtVencRec.setText(rs.getString(11));
                cboBanco.setSelectedItem(rs.getString(12));
                lblIdSinistro.setText(rs.getString(13));
                lblIdVeiculo.setText(rs.getString(14));
                lblIdAssociado.setText(rs.getString(15));
                txtIdEmpresa.setText(rs.getString(16));  
                txtNomeAssocSinistro.setText(rs.getString(17));
                lblMensalidade.setText(rs.getString(18));

                if (cboTipoDeBaixa.getSelectedItem().toString().equals("Total")) {
                    verificarAtraso();
                } else if (cboTipoDeBaixa.getSelectedItem().toString().equals("Parcial")) {
                    verificarAtrasoPagParcial();
                }

                btnAdicionar.setEnabled(true);
                btnAlterar.setEnabled(true);
                btnRemover.setEnabled(true);
                btnPesquisar.setEnabled(true);
                btnImprimir.setEnabled(true);

                btnLimpar.setEnabled(true);

                btnDesfazerBaixa.setEnabled(false);
                cboTipoDeBaixa.setSelectedIndex(0);

                listaDeConjuntosAssoc();
                pesquisar_baixas();

            } else {
                JOptionPane.showMessageDialog(null, "Não existe conta para este associado.");
                limpar();

            }
        } catch (SQLSyntaxErrorException e) {
            JOptionPane.showMessageDialog(null, "ID Inválido");
            //System.out.println(e);
        } catch (Exception e2) {
            JOptionPane.showMessageDialog(null, e2);
        }
        //A linha abaixo desabilita o botão adicionar
        btnAdicionar.setEnabled(false);
    }

    private void consultar() {

        String idsinistro = "";
        String idveiculo = "";
        String idassociado = "";

        String id_rec = JOptionPane.showInputDialog("Informe o ID do Associado?");

        String sql = "select idrec, nossonumero, descricao, associado, Concat(   \n"
                + "               Replace  \n"
                + "                 (Replace  \n"
                + "                   (Replace  \n"
                + "                     (Format(valorprinc, 3), '.', '|'), ',', '.'), '|', ',')) AS Total"
                + " ,Concat(   \n"
                + "               Replace  \n"
                + "                 (Replace  \n"
                + "                   (Replace  \n"
                + "                     (Format(valorrestante, 3), '.', '|'), ',', '.'), '|', ',')) as Restante,"
                + " parcelas, pago, formapag, date_format(dataemissao,'%d/%m/%Y'), date_format(datavenc,'%d/%m/%Y'),banco, idsin, idveic, idassoc, idempr, nomeassocsinistro from tbcontasreceber where contaexcluida  = 0 and idrec =" + id_rec;

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                txtIdRec.setText(rs.getString(1));
                txtNossoNum.setText(rs.getString(2));
                txtDescricaoRec.setText(rs.getString(3));
                txtNomeAssoc.setText(rs.getString(4));
                txtValorPrinc.setText(rs.getString(5));
                txtValorRest.setText(rs.getString(6));
                txtParc.setText(rs.getString(7));
                labelStatusRec.setText(rs.getString(8));

                String status = labelStatusRec.getText();

                if ("0".equals(status)) {
                    labelStatusRec.setText("Aberto");
                } else {
                    labelStatusRec.setText("Pago");
                }
                cboFormaDePag.setSelectedItem(rs.getString(9));
                txtDataEmissao.setText(rs.getString(10));
                txtVencRec.setText(rs.getString(11));
                cboBanco.setSelectedItem(rs.getString(12));
                lblIdSinistro.setText(rs.getString(13));
                lblIdVeiculo.setText(rs.getString(14));
                lblIdAssociado.setText(rs.getString(15));
                txtIdEmpresa.setText(rs.getString(16));

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

        String dia = txtVencRec.getText().substring(0, 2);
        String mes = txtVencRec.getText().substring(3, 5);
        String ano = txtVencRec.getText().substring(6);

        String datamysql = ano + "-" + mes + "-" + dia;

        String diaEmi = txtDataEmissao.getText().substring(0, 2);
        String mesEmi = txtDataEmissao.getText().substring(3, 5);
        String anoEmi = txtDataEmissao.getText().substring(6);

        String dataEmimysql = anoEmi + "-" + mesEmi + "-" + diaEmi;

        String sql = "update tbcontasreceber set nossonumero=?, descricao=?, associado=?, valorprinc=?, valorrestante=?, parcelas=?, formapag=?, dataemissao=?, datavenc=?, banco= ?, nomeassocsinistro = ? where idrec = ?";

        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, txtNossoNum.getText());
            pst.setString(2, txtDescricaoRec.getText());
            pst.setString(3, txtNomeAssoc.getText());
            pst.setString(4, txtValorPrinc.getText().replace(".", "").replace(",", "."));
            pst.setString(5, txtValorRest.getText().replace(".", "").replace(",", "."));
            pst.setString(6, txtParc.getText());
            pst.setString(7, cboFormaDePag.getSelectedItem().toString());
            pst.setString(8, dataEmimysql);
            pst.setString(9, datamysql);
            pst.setString(10, cboBanco.getSelectedItem().toString());
            pst.setString(11, txtNomeAssocSinistro.getText());
            pst.setString(12, txtIdRec.getText());

            //Validação dos campos obrigatorios
            if ((txtNomeAssoc.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatorios.");

            } else {
                int adicionado = pst.executeUpdate();

                //System.out.println(adicionado);
                if (adicionado > 0) {

                    JOptionPane.showMessageDialog(null, "Dados da conta alterado com sucesso");

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

        if (labelStatusRec.getText().equals("Aberto")) {
            int confirma = JOptionPane.showConfirmDialog(null, "Tem Certeza que deseja excluir esta conta.", "Atenção", JOptionPane.YES_NO_OPTION);

            if (confirma == JOptionPane.YES_OPTION) {
                String sql = "update tbcontasreceber set contaexcluida = 1 where pago = 0 and idrec = ?";

                try {
                    pst = conexao.prepareStatement(sql);
                    pst.setString(1, txtIdRec.getText());
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

        } else {
            JOptionPane.showMessageDialog(null, "Conta paga, não é possivel excluir.");
        }

    }

    public void limpar() {
        txtIdRec.setText(null);
        txtNossoNum.setText(null);
        txtDescricaoRec.setText(null);
        txtNomeAssoc.setText(null);
        txtValorPrinc.setText(null);
        txtValorRest.setText(null);
        txtParc.setText(null);
        cboFormaDePag.setSelectedIndex(0);
        txtDataEmissao.setText(null);
        txtVencRec.setText(null);
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

    private void imprimirdoc_adesao() {

        int confirma = JOptionPane.showConfirmDialog(null, "Imprimir documento de adesão?", "Atenção", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {
            //Imprimir relatorio com o framework JasperReport
            try {
                // usando a classe HashMap para criar um filtro
                HashMap filtro = new HashMap();
                filtro.put("idassoc", Integer.parseInt(txtIdRec.getText()));
                JasperPrint print = JasperFillManager.fillReport(getClass().getResourceAsStream("/impressao/termodeadesao.jasper"), filtro, conexao);
                //a linha abaixo apresenta o relatorio através da classe JasperViewer
                JasperViewer.viewReport(print, false);

            } catch (JRException e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    private void buscaUltimoId() {

        String sql = "select idrec from tbcontasreceber order by idrec desc limit 1;";

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                txtIdRec.setText(rs.getString(1));

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

    public void listaDePesquisaAssociadoSinistro() {

        String sql = "select * from tbassociado where associadoexcluido = 0 and NOMEASSOC like '" + txtNomeAssocSinistro.getText() + "%' ORDER BY NOMEASSOC";

        try {
            pst = conexao.prepareStatement(sql);
            MODASSOCSIN.removeAllElements();
            int v = 0;

            rs = pst.executeQuery();
            while (rs.next() & v < 8) {
                MODASSOCSIN.addElement(rs.getString(3));
                v++;
            }

            if (v >= 1) {
                listAssocSinistro.setVisible(true);
            } else {
                listAssocSinistro.setVisible(false);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao listar dados" + e);
        }
    }

    public void listaCampoPesquisaAssociado() {

        String sql = "select * from tbassociado where associadoexcluido = 0 and NOMEASSOC like '" + txtNomeAssoc.getText() + "%' ORDER BY NOMEASSOC";

        try {
            pst = conexao.prepareStatement(sql);
            MODELOPESQASSOC.removeAllElements();
            int v = 0;

            rs = pst.executeQuery();
            while (rs.next() & v < 4) {
                MODELOPESQASSOC.addElement(rs.getString(3));
                v++;
            }

            if (v >= 1) {
                listPesquisarAssociaodo.setVisible(true);
            } else {
                listPesquisarAssociaodo.setVisible(false);
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

    private void mostrarPesquisaAssociadoSinistro() {

        String Linha = listAssocSinistro.getSelectedValue();

        txtNomeAssocSinistro.setText(Linha);

    }

    private void mostrarAssociadoCampoPesquisa() {

        String Linha = listPesquisarAssociaodo.getSelectedValue();

        txtAssocPesquisar.setText(Linha);

    }

    public void mostrarPesquisaAssociado2() {

        int Linha = listAssociado.getSelectedIndex();

        if (Linha >= 0) {
            String sql = "select * from tbassociado where associadoexcluido = 0 and NOMEASSOC like '" + txtNomeAssoc.getText() + "%' ORDER BY NOMEASSOC limit " + Linha + ", 1";

            try {
                pst = conexao.prepareStatement(sql);
                MODELOASSOC.removeAllElements();

                rs = pst.executeQuery();

                txtNomeAssoc.setText(rs.getString(3));

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Erro ao mostrar" + e);
            }

        }
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

    private void setarJurosBanco() {

        String nomebanco = cboBanco.getSelectedItem().toString();

        String sql = "select * from tbbancos where nomebanco = " + "'" + nomebanco + "'";

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                txtJurosRec.setText(rs.getString(3).replace(",", "").replace(".", ","));

            }
        } catch (Exception e2) {
            JOptionPane.showMessageDialog(null, e2);
        }

    }

    private void setarMultaBanco() {

        String nomebanco = cboBanco.getSelectedItem().toString();

        String sql = "select * from tbbancos where nomebanco = " + "'" + nomebanco + "'";

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                txtMultaRec.setText(rs.getString(4).replace(",", "").replace(".", ","));

            }
        } catch (Exception e2) {
            JOptionPane.showMessageDialog(null, e2);
        }

    }

    private void listaDeConjuntosAssoc() {

        String sql = "select idveic as ID, placa as Placa,  marca as Marca, modelo as Modelo, date_format(data_cadveic,'%d/%m/%Y') as Cadastro from tbveiculo where and veiculoexcluido = 0 and idassoc = ?";

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

    //processo de baixa
    ///////////////////////////////////////////////////////////////////////
    private void adicionarBaixaTotal1() {

        String sql = "insert into tbreceberbaixas(valordabaixa, juros, multa, formapag, idrec) values(?,?,?,?,?)";
        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, txtValorPrinc.getText().replace(".", "").replace(",", "."));
            pst.setString(2, txtJurosRec.getText());
            pst.setString(3, txtMultaRec.getText());
            pst.setString(4, cboFormaDePag.getSelectedItem().toString());
            pst.setString(5, txtIdRec.getText());

            //Validação dos campos obrigatorios
            if ((txtNomeAssoc.getText().isEmpty()) || (txtValorPrinc.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatorios.");

            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void adicionarBaixaParcial1() {

        valorbaixaparcial = JOptionPane.showInputDialog("Informe o valor da baixa parcial?");
        //String input = JOptionPane.showInputDialog("Enter your height in centimeters:");
        while (!valorbaixaparcial.matches("\\d+")) {

            valorbaixaparcial = JOptionPane.showInputDialog("Valor da baixa parcial somente numeros e virgula");
        }
        valorbaixaparcialsonum = Integer.parseInt(valorbaixaparcial);

        if (valorbaixaparcial != "") {

            String sql = "insert into tbreceberbaixas(valordabaixa, juros, multa, formapag, idrec) values(?,?,?,?,?)";

            try {

                pst2 = conexao.prepareStatement(sql);

                pst2.setString(1, valorbaixaparcial.replace(".", "").replace(",", "."));
                pst2.setString(2, txtJurosRec.getText());
                pst2.setString(3, txtMultaRec.getText());
                pst2.setString(4, cboFormaDePag.getSelectedItem().toString());
                pst2.setString(5, txtIdRec.getText());

                //Validação dos campos obrigatorios
                if ((txtNomeAssoc.getText().isEmpty()) || (valorbaixaparcial.isEmpty()) & (valorbaixaparcial.matches("^[0-9]+$"))) {
                    JOptionPane.showMessageDialog(null, "Valor invalido");

                } else {
                    int adicionado = pst2.executeUpdate();

                    //System.out.println(adicionado);
                    if (adicionado > 0) {

                        /* JOptionPane.showMessageDialog(null, "Conta a receber adicionado com sucesso");

                    btnAdicionar.setEnabled(false);
                    btnAlterar.setEnabled(true);
                    btnRemover.setEnabled(true);
                    btnPesquisar.setEnabled(true);
                    btnImprimir.setEnabled(true);
                   

                    buscaUltimoId(); */
                        buscaUltimaBaixaId();

                        updateValoRestanteBaixaParc();

                    }
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }

    }

    private void baixarConta1() {

        int confirma = JOptionPane.showConfirmDialog(null, "Executar baixa total?", "Atenção", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.NO_OPTION) {

            adicionarBaixaParcial();

        } else if (confirma == JOptionPane.YES_OPTION) {

            adicionarBaixaTotal();

            String sql = "update tbcontasreceber set pago = 1, valorrestante = 0  where idrec = ?";

            try {

                pst = conexao.prepareStatement(sql);

                pst.setString(1, txtIdRec.getText());

                //Validação dos campos obrigatorios
                int adicionado = pst.executeUpdate();

                //System.out.println(adicionado);
                if (adicionado > 0) {

                    JOptionPane.showMessageDialog(null, "Baixa executada com sucesso!");

                    limpar();

                    btnAdicionar.setEnabled(false);
                    btnAlterar.setEnabled(true);
                    btnRemover.setEnabled(true);
                    btnPesquisar.setEnabled(true);
                    btnImprimir.setEnabled(true);

                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }

    }
    ///////////////////////////////////////////////////////////////////////

    private void adicionarBaixaTotal() {

        String dia = txtDataBaixaRec.getText().substring(0, 2);
        String mes = txtDataBaixaRec.getText().substring(3, 5);
        String ano = txtDataBaixaRec.getText().substring(6);

        String datamysqlrecbaixa = ano + "-" + mes + "-" + dia;

        int diasatraso = Integer.parseInt(lblDiasAtrasoIf.getText());

        String sql = "insert into tbreceberbaixas(valordabaixa, juros, multa, formapag, idrec, datadabaixa, valorjurosmulta) values(?,?,?,?,?,?,?)";
        try {

            pst1 = conexao.prepareStatement(sql);

            pst1.setString(1, txtValorBaixaRec.getText().replace(".", "").replace(",", "."));
            pst1.setString(2, txtJurosBaixa.getText().replace(".", "").replace(",", "."));
            pst1.setString(3, txtMultaBaixa.getText().replace(".", "").replace(",", "."));
            pst1.setString(4, cboFormaDePag.getSelectedItem().toString());
            pst1.setString(5, txtIdRec.getText());
            pst1.setString(6, datamysqlrecbaixa);
            if (diasatraso > 0) {
                pst1.setString(7, String.valueOf(new DecimalFormat("#,##0.00").format(valormultajuros)).replace(".", "").replace(",", "."));
            } else {
                pst1.setString(7, "0");
            }

            //Validação dos campos obrigatorios           
            if ((txtNomeAssoc.getText().isEmpty()) || (txtValorPrinc.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null, "Valor invalido");

            } else {
                int adicionado = pst1.executeUpdate();

                int mensal = Integer.parseInt(lblMensalidade.getText());

                if (mensal == 1) {
                    criarUltimaMensalidadeAltomatica();
                }

                mudarStatusPago();

            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void adicionarBaixaParcial() {

        String dia = txtDataBaixaRec.getText().substring(0, 2);
        String mes = txtDataBaixaRec.getText().substring(3, 5);
        String ano = txtDataBaixaRec.getText().substring(6);

        String datamysqlrecbaixaparc = ano + "-" + mes + "-" + dia;

        int diasatraso = Integer.parseInt(lblDiasAtrasoIf.getText());

        valorbaixaparcial = txtValorBaixaRec.getText();

        if (!"".equals(valorbaixaparcial)) {

            String sql = "insert into tbreceberbaixas(valordabaixa, juros, multa, formapag, idrec, datadabaixa, valorjurosmulta) values(?,?,?,?,?,?,?)";

            try {

                pst2 = conexao.prepareStatement(sql);

                pst2.setString(1, valorbaixaparcial.replace(".", "").replace(",", "."));
                pst2.setString(2, txtJurosBaixa.getText().replace(".", "").replace(",", "."));
                pst2.setString(3, txtMultaBaixa.getText().replace(".", "").replace(",", "."));
                pst2.setString(4, cboFormaDePag.getSelectedItem().toString());
                pst2.setString(5, txtIdRec.getText());
                pst2.setString(6, datamysqlrecbaixaparc);
                if (diasatraso > 0) {
                    pst2.setString(7, String.valueOf(new DecimalFormat("#,##0.00").format(valormultajuros)).replace(".", "").replace(",", "."));
                } else {
                    pst1.setString(7, "0");
                }

                //Validação dos campos obrigatorios
                if ((txtNomeAssoc.getText().isEmpty()) || (valorbaixaparcial.isEmpty()) & (valorbaixaparcial.matches("^[0-9]+$"))) {
                    JOptionPane.showMessageDialog(null, "Valor invalido");

                } else {
                    int adicionado = pst2.executeUpdate();

                    //System.out.println(adicionado);
                    if (adicionado > 0) {

                        /* JOptionPane.showMessageDialog(null, "Conta a receber adicionado com sucesso");

                    btnAdicionar.setEnabled(false);
                    btnAlterar.setEnabled(true);
                    btnRemover.setEnabled(true);
                    btnPesquisar.setEnabled(true);
                    btnImprimir.setEnabled(true);
                   

                    buscaUltimoId(); */
                        buscaUltimaBaixaId();

                        updateValoRestanteBaixaParc();
                        //deduzirValorRestante();

                    }
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }

    }

    private void deduzirValorRestante() {

        String valordabaixa = txtValorBaixaRec.getText();

        String sql = "update tbcontasreceber set valorrestante = valorrestante - valorrestante  where idrec = ?";

        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, txtIdRec.getText());

            //Validação dos campos obrigatorios
            int adicionado = pst.executeUpdate();

            //System.out.println(adicionado);
            if (adicionado > 0) {

                JOptionPane.showMessageDialog(null, "Baixa executada com sucesso!");

                limpar();

                btnAdicionar.setEnabled(false);
                btnAlterar.setEnabled(true);
                btnRemover.setEnabled(true);
                btnPesquisar.setEnabled(true);
                btnImprimir.setEnabled(true);

            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void baixarConta() {

        String totalouparcial = cboTipoDeBaixa.getSelectedItem().toString();
        Double valordabaixa = new Double(txtValorBaixaRec.getText().replace(".", "").replace(",", "."));
        Double valorestante = new Double(txtValorRest.getText().replace(".", "").replace(",", "."));

        if (totalouparcial.equals("Total")) {

            adicionarBaixaTotal();

        }
        if (totalouparcial.equals("Parcial")) {
            if (valordabaixa < valorestante) {
                adicionarBaixaParcial();
            } else if (valordabaixa.equals(valorestante)) {

                adicionarBaixaTotal();

            } else {
                JOptionPane.showMessageDialog(null, "Valor da baixar maior que o valor restante!");
            }

        }

    }

    private void mudarStatusPago() {

        String sql = "update tbcontasreceber set pago = 1, valorrestante = 0  where idrec = ?";

        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, txtIdRec.getText());

            //Validação dos campos obrigatorios
            int adicionado = pst.executeUpdate();

            //System.out.println(adicionado);
            if (adicionado > 0) {

                JOptionPane.showMessageDialog(null, "Baixa executada com sucesso!");

                limpar();

                btnAdicionar.setEnabled(false);
                btnAlterar.setEnabled(true);
                btnRemover.setEnabled(true);
                btnPesquisar.setEnabled(true);
                btnImprimir.setEnabled(true);

            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void buscaUltimaBaixaId() {

        String sql = "select * from tbreceberbaixas order by idbaixa desc limit 1;";

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                idultimabaixa = rs.getString(1);

            }
        } catch (Exception e2) {
            JOptionPane.showMessageDialog(null, e2);
        }
    }

    private void verifcarPagamentoTotalNaBaixaParc() {

        String sql = "update tbcontasreceber set pago = 1 where idrec = ? and valorrestante = 0 ";

        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, txtIdRec.getText());

            //Validação dos campos obrigatorios
            int adicionado = pst.executeUpdate();

            //System.out.println(adicionado);
            if (adicionado > 0) {

                //JOptionPane.showMessageDialog(null, "Baixa executada com sucesso!");

                /*limpar();

                    btnAdicionar.setEnabled(false);
                    btnAlterar.setEnabled(true);
                    btnRemover.setEnabled(true);
                    btnPesquisar.setEnabled(true);
                    btnImprimir.setEnabled(true);*/
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void updateValoRestanteBaixaParc() {

        String sql = "update tbcontasreceber set valorrestante = (valorrestante + (select valorjurosmulta from tbreceberbaixas where idbaixa = ?) - (select valordabaixa from tbreceberbaixas where idbaixa = ?)) where idrec = ?";

        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, idultimabaixa);
            pst.setString(2, idultimabaixa);
            pst.setString(3, txtIdRec.getText());

            //Validação dos campos obrigatorios
            int adicionado = pst.executeUpdate();

            //System.out.println(adicionado);
            if (adicionado > 0) {

                verifcarPagamentoTotalNaBaixaParc();

                JOptionPane.showMessageDialog(null, "Baixa executada com sucesso!");

                limpar();

                btnAdicionar.setEnabled(false);
                btnAlterar.setEnabled(true);
                btnRemover.setEnabled(true);
                btnPesquisar.setEnabled(true);
                btnImprimir.setEnabled(true);

            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    //Tabela das baixas 
    private void pesquisar_baixas() {

        String sql = "select idbaixa as ID, valordabaixa as Baixa, date_format(datadabaixa,'%d/%m/%Y') as Data, formapag as Forma, juros AS Juros, multa as Multa from tbreceberbaixas where idrec = ?";

        try {
            pst = conexao.prepareStatement(sql);
            //Passando o conteudo da caixa de pesquisa para o ?
            //atenção ao "%" - continuação da pesquisa sql
            pst.setString(1, txtIdRec.getText() + "%");
            rs = pst.executeQuery();
            // a linha abaixo usa a biblioteca rs2xml.jar para preencher a tabela 
            tblBaixasRecebidas.setModel(DbUtils.resultSetToTableModel(rs));

            tblBaixasRecebidas.getColumnModel().getColumn(0).setMaxWidth(50);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    public void setar_campos_da_baixa() {

        int setar = tblBaixasRecebidas.getSelectedRow();

        lblIdbaixa.setText(tblBaixasRecebidas.getModel().getValueAt(setar, 0).toString());

        String id_baixa = lblIdbaixa.getText();

        String sql = "select idbaixa, Concat( Replace  \n"
                + "                              (Replace \n"
                + "                                  (Replace  \n"
                + "                                  (Format(valordabaixa, 3), '.', '|'), ',', ''), '|', ',')) as Baixa, juros, multa, formapag, idrec, date_format(datadabaixa,'%d/%m/%Y') from tbreceberbaixas where idbaixa =" + id_baixa;

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {

                lblIdbaixa.setText(rs.getString(1));
                txtValorBaixaRec.setText(rs.getString(2));
                txtJurosBaixa.setText(rs.getString(3));
                txtMultaBaixa.setText(rs.getString(4));
                cboFormaDePag.setSelectedItem(rs.getString(5));
                txtIdRec.setText(rs.getString(6));
                txtDataBaixaRec.setText(rs.getString(7));

                btnAdicionar.setEnabled(true);
                btnAlterar.setEnabled(true);
                btnRemover.setEnabled(true);
                btnPesquisar.setEnabled(true);
                btnImprimir.setEnabled(true);
                btnLimpar.setEnabled(true);
                btnDesfazerBaixa.setEnabled(true);

                //verificarAtraso();
                //verificarAtrasoPagParcial();
            } else {
                JOptionPane.showMessageDialog(null, "Não existe baixa(s) para esta conta.");
                limpar();

            }
        } catch (SQLSyntaxErrorException e) {
            JOptionPane.showMessageDialog(null, "ID Inválido");
            //System.out.println(e);
        } catch (Exception e2) {
            JOptionPane.showMessageDialog(null, e2);
        }
        //A linha abaixo desabilita o botão adicionar
        btnAdicionar.setEnabled(false);
    }

    //Desfazer baixa
    private void retornaValorBaixa() {

        Double valordabaixa = new Double(txtValorBaixaRec.getText().replace(".", "").replace(",", "."));
        Double valorestante = new Double(txtValorRest.getText().replace(".", "").replace(",", "."));

        int confirma = JOptionPane.showConfirmDialog(null, "Desfazer baixa.", "Atenção", JOptionPane.YES_NO_OPTION);

        int diasatraso = Integer.parseInt(lblDiasAtrasoIf.getText());

        System.out.println(totaldiasatraso);

        if (confirma == JOptionPane.YES_OPTION) {
            if (valorestante == 0) {
                if (diasatraso > 0) {
                    retornaValorBaixadoComStatusComJuros();
                    System.out.println("Com Juros");
                } else {
                    retornaValorBaixadoComStatus();
                    System.out.println("Sem Juros");
                }
            } else {
                if (diasatraso > 0) {
                    retornaValorBaixadoSemStatusComJuros();
                    System.out.println("Com Juros");
                } else {
                    retornaValorBaixadoSemStatus();
                    System.out.println("Sem Juros");
                }
            }
        }
    }

    private void retornaValorBaixadoSemStatus() {

        String sql = "update tbcontasreceber set valorrestante = (valorrestante + (select valordabaixa from tbreceberbaixas where idbaixa = ?)) where idrec = ?";

        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, lblIdbaixa.getText());
            pst.setString(2, txtIdRec.getText());

            //Validação dos campos obrigatorios
            int adicionado = pst.executeUpdate();

            if (adicionado > 0) {

                deletaBaixa();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void retornaValorBaixadoComStatus() {

        String sql = "update tbcontasreceber set valorrestante = (valorrestante + (select valordabaixa from tbreceberbaixas where idbaixa = ?)), pago = 0 where idrec = ?";

        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, lblIdbaixa.getText());
            pst.setString(2, txtIdRec.getText());

            //Validação dos campos obrigatorios
            int adicionado = pst.executeUpdate();

            if (adicionado > 0) {

                deletaBaixa();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void retornaValorBaixadoComStatusComJuros() {

        String sql = "update tbcontasreceber set valorrestante = (valorrestante + (select (valordabaixa - valorjurosmulta) from tbreceberbaixas where idbaixa = ?)), pago = 0 where idrec = ?;";

        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, lblIdbaixa.getText());
            pst.setString(2, txtIdRec.getText());

            //Validação dos campos obrigatorios
            int adicionado = pst.executeUpdate();

            if (adicionado > 0) {

                deletaBaixa();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void retornaValorBaixadoSemStatusComJuros() {

        String sql = "update tbcontasreceber set valorrestante = (valorrestante + (select (valordabaixa - valorjurosmulta) from tbreceberbaixas where idbaixa = ?)) where idrec = ?";

        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, lblIdbaixa.getText());
            pst.setString(2, txtIdRec.getText());

            //Validação dos campos obrigatorios
            int adicionado = pst.executeUpdate();

            if (adicionado > 0) {

                deletaBaixa();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void deletaBaixa() {

        String sql = "delete from tbreceberbaixas where idbaixa=?";

        try {
            pst1 = conexao.prepareStatement(sql);
            pst1.setString(1, lblIdbaixa.getText());
            int apagado = pst1.executeUpdate();

            if (apagado > 0) {
                JOptionPane.showMessageDialog(null, "Baixa desfeita com sucesso");

            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void verificarAtraso() {

        int diaVenc = Integer.parseInt(txtVencRec.getText().substring(0, 2));
        int mesVenc = Integer.parseInt(txtVencRec.getText().substring(3, 5));
        int anoVenc = Integer.parseInt(txtVencRec.getText().substring(6));

        LocalDate datavencimento = LocalDate.of(anoVenc, mesVenc, diaVenc);

        int diaBaixa = Integer.parseInt(txtDataBaixaRec.getText().substring(0, 2));
        int mesBaixa = Integer.parseInt(txtDataBaixaRec.getText().substring(3, 5));
        int anoBaixa = Integer.parseInt(txtDataBaixaRec.getText().substring(6));

        LocalDate databaixa = LocalDate.of(anoBaixa, mesBaixa, diaBaixa);

        int totaldiasatraso = (int) ChronoUnit.DAYS.between(datavencimento, databaixa);

        lblDiasDeAtraso.setText("");
        lblDiasAtrasoIf.setText("0");

        if (totaldiasatraso > 0) {

            checkCobraJurosMulta.setSelected(true);

            Double valordabaixa = new Double(txtValorBaixaRec.getText().replace(".", "").replace(",", "."));
            Double valorestante = new Double(txtValorRest.getText().replace(".", "").replace(",", "."));
            Double juros = new Double(txtJurosRec.getText().replace(".", "").replace(",", "."));
            Double multa = new Double(txtMultaRec.getText().replace(".", "").replace(",", "."));

            Double jurosdia = juros / 30;

            Double totaljuros = (jurosdia / 100) * totaldiasatraso;

            Double valorjuros = valorestante * totaljuros;

            Double valormulta = (multa / 100) * valorestante;

            Double valormultajuroserestante = valorjuros + valormulta + valorestante;

            valormultajuros = valorjuros + valormulta;

            // System.out.println(valormultajuroserestante);
            txtValorBaixaRec.setText(String.valueOf(new DecimalFormat("#,##0.00").format(valormultajuroserestante)));

            txtMultaBaixa.setText(txtMultaRec.getText());
            txtJurosBaixa.setText(txtJurosRec.getText());
            if (labelStatusRec.getText().equals("Aberto")) {
                lblDiasDeAtraso.setText(totaldiasatraso + " dia(s) de atraso.");
            }
            lblDiasAtrasoIf.setText(String.valueOf(totaldiasatraso));

        } else {
            checkCobraJurosMulta.setSelected(false);
            txtJurosBaixa.setText("0,0");
            txtMultaBaixa.setText("0,0");
        }

    }

    private void verificarAtrasoPagParcial() {

        int diaVenc = Integer.parseInt(txtVencRec.getText().substring(0, 2));
        int mesVenc = Integer.parseInt(txtVencRec.getText().substring(3, 5));
        int anoVenc = Integer.parseInt(txtVencRec.getText().substring(6));

        LocalDate datavencimento = LocalDate.of(anoVenc, mesVenc, diaVenc);

        int diaBaixa = Integer.parseInt(txtDataBaixaRec.getText().substring(0, 2));
        int mesBaixa = Integer.parseInt(txtDataBaixaRec.getText().substring(3, 5));
        int anoBaixa = Integer.parseInt(txtDataBaixaRec.getText().substring(6));

        LocalDate databaixa = LocalDate.of(anoBaixa, mesBaixa, diaBaixa);

        totaldiasatraso = (int) ChronoUnit.DAYS.between(datavencimento, databaixa);

        lblDiasDeAtraso.setText("");
        lblDiasAtrasoIf.setText("0");

        if (totaldiasatraso > 0) {

            checkCobraJurosMulta.setSelected(true);

            Double valordabaixa = new Double(txtValorBaixaRec.getText().replace(".", "").replace(",", "."));
            Double valorestante = new Double(txtValorRest.getText().replace(".", "").replace(",", "."));
            Double juros = new Double(txtJurosRec.getText().replace(".", "").replace(",", "."));
            Double multa = new Double(txtMultaRec.getText().replace(".", "").replace(",", "."));

            Double jurosdia = juros / 30;

            Double totaljuros = (jurosdia / 100) * totaldiasatraso;

            Double valorjuros = valordabaixa * totaljuros;

            Double valormulta = (multa / 100) * valordabaixa;

            Double valormultajuroserestante = valorjuros + valormulta + valordabaixa;

            valormultajuros = valorjuros + valormulta;

            // System.out.println(valormultajuroserestante);
            txtValorBaixaRec.setText(String.valueOf(new DecimalFormat("#,##0.00").format(valormultajuroserestante)));

            txtMultaBaixa.setText(txtMultaRec.getText());
            txtJurosBaixa.setText(txtJurosRec.getText());
            if (labelStatusRec.getText().equals("Aberto")) {
                lblDiasDeAtraso.setText(totaldiasatraso + " dia(s) de atraso.");
            }
            lblDiasAtrasoIf.setText(String.valueOf(totaldiasatraso));

        } else {
            checkCobraJurosMulta.setSelected(false);
            txtJurosBaixa.setText("0,0");
            txtMultaBaixa.setText("0,0");
        }

    }

    private void tornaInvisivel() {
        lblIdSinistro.setVisible(false);
        lblIdVeiculo.setVisible(false);
        lblIdAssociado.setVisible(false);
        txtIdEmpresa.setVisible(false);
        lblStatusContaExcluida.setVisible(false);
        listAssociado.setVisible(false);
        listAssocSinistro.setVisible(false);
        listPesquisarAssociaodo.setVisible(false);
        lblIdbaixa.setVisible(false);
        lblDiasAtrasoIf.setVisible(false);
    }

    // criar parcela altomatica apos a ultima gerada       
    private void criarUltimaMensalidadeAltomatica() {

        String status = labelStatusRec.getText();
        int statusnum = 0;

        if (status == "Pago") {
            statusnum = 1;
        }

        setarDataMaisAntiga();
        setarMaiorParcela();

        Date date = Date.valueOf(datamaisantiga);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String dataantiga = format.format(date);

        System.out.println(dataantiga);

        txtVencRec.setText(dataantiga);

        int diaParMaior = Integer.parseInt(txtVencRec.getText().substring(0, 2));
        int mesParMaior = Integer.parseInt(txtVencRec.getText().substring(3, 5));
        int anoParMaior = Integer.parseInt(txtVencRec.getText().substring(6));

        localdatavencimento = LocalDate.of(anoParMaior, mesParMaior, diaParMaior);

        if (mesParMaior == 12) {
            mesParMaior = 1;
            // somar 1 ano = 28 de fevereiro de 2017
            localdatavencimento = localdatavencimento.plusYears(1);

            //datamaisantiga = String.valueOf(localdatavencimento);
            Date date2 = Date.valueOf(localdatavencimento);
            SimpleDateFormat format2 = new SimpleDateFormat("dd/MM/yyyy");
            String data22 = format.format(date2);

            txtVencRec.setText(data22);

            diaParMaior = Integer.parseInt(txtVencRec.getText().substring(0, 2));
            mesParMaior = 1;
            anoParMaior = Integer.parseInt(txtVencRec.getText().substring(6));

        } else {
            mesParMaior++;
        }

        String datamaior = String.valueOf(anoParMaior) + "-" + String.valueOf(mesParMaior) + "-" + String.valueOf(diaParMaior);

        System.out.println(datamaior);

        maiorparcela++;

        txtParc.setText(String.valueOf(maiorparcela));

        String sql = "insert into tbcontasreceber(nossonumero, descricao, associado, valorprinc, valorrestante, parcelas, pago, formapag, datavenc, banco, idsin, idveic, idassoc,idempr, nomeassocsinistro, contaexcluida, mensalidade) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, "000");
            pst.setString(2, "Mensalidade do Associado.");
            pst.setString(3, txtNomeAssoc.getText());
            pst.setString(4, txtValorPrinc.getText().replace(".", "").replace(",", "."));
            pst.setString(5, txtValorPrinc.getText().replace(".", "").replace(",", "."));
            pst.setString(6, txtParc.getText());
            pst.setString(7, String.valueOf(statusnum));
            pst.setString(8, cboFormaDePag.getSelectedItem().toString());
            pst.setString(9, datamaior);
            pst.setString(10, cboBanco.getSelectedItem().toString());
            pst.setString(11, "0");
            pst.setString(12, "0");
            pst.setString(13, lblIdAssociado.getText());
            pst.setString(14, txtIdEmpresa.getText());
            pst.setString(15, " ");
            pst.setString(16, lblStatusContaExcluida.getText());
            pst.setString(17, "1");

            int adicionado = pst.executeUpdate();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void setarDataMaisAntiga() {
        String sql = "select datavenc from tbcontasreceber where idassoc = ? and idrec = (select idrec from tbcontasreceber where idassoc = ?  and mensalidade = 1 order by idrec desc limit 1);";

        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, lblIdAssociado.getText());
            pst.setString(2, lblIdAssociado.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                datamaisantiga = rs.getString(1);

            }
        } catch (Exception e2) {
            JOptionPane.showMessageDialog(null, e2);
        }
    }

    private void setarMaiorParcela() {
        String sql = "select parcelas from tbcontasreceber where idassoc = ? and idrec = (select idrec from tbcontasreceber where idassoc = ?  and mensalidade = 1 order by idrec desc limit 1);";

        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, lblIdAssociado.getText());
            pst.setString(2, lblIdAssociado.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                maiorparcela = Integer.parseInt(rs.getString(1));

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
        txtAssocPesquisar = new javax.swing.JTextField();
        listPesquisarAssociaodo = new javax.swing.JList<>();
        jLabel6 = new javax.swing.JLabel();
        btnRemover = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        txtIdRec = new javax.swing.JTextField();
        btnImprimir = new javax.swing.JButton();
        lblCnpjCpf = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblAssociadosReceber = new javax.swing.JTable();
        btnPesquisar = new javax.swing.JButton();
        btnLimpar = new javax.swing.JButton();
        jLabel19 = new javax.swing.JLabel();
        txtIdEmpresa = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        txtValorRest = new javax.swing.JFormattedTextField();
        jLabel22 = new javax.swing.JLabel();
        cboFormaDePag = new javax.swing.JComboBox<>();
        txtNossoNum = new javax.swing.JTextField();
        txtDataEmissao = new javax.swing.JFormattedTextField();
        jLabel23 = new javax.swing.JLabel();
        txtDescricaoRec = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        txtVencRec = new javax.swing.JFormattedTextField();
        labelStatusRec = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        cboBanco = new javax.swing.JComboBox<>();
        lblIdSinistro = new javax.swing.JLabel();
        lblIdVeiculo = new javax.swing.JLabel();
        lblIdAssociado = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtNomeAssocSinistro = new javax.swing.JTextField();
        listAssocSinistro = new javax.swing.JList<>();
        lblStatusContaExcluida = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtMultaRec = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtJurosRec = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblVeiculosAssoc = new javax.swing.JTable();
        txtParc = new javax.swing.JTextField();
        txtValorPrinc = new javax.swing.JFormattedTextField();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblBaixasRecebidas = new javax.swing.JTable();
        painelBaixa = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        txtValorBaixaRec = new javax.swing.JFormattedTextField();
        jLabel10 = new javax.swing.JLabel();
        txtDataBaixaRec = new javax.swing.JFormattedTextField();
        jLabel27 = new javax.swing.JLabel();
        cboFormaDePagBaixa = new javax.swing.JComboBox<>();
        jLabel11 = new javax.swing.JLabel();
        txtMultaBaixa = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txtJurosBaixa = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        cboTipoDeBaixa = new javax.swing.JComboBox<>();
        btnBaixcarConta = new javax.swing.JButton();
        checkCobraJurosMulta = new javax.swing.JCheckBox();
        btnDesfazerBaixa = new javax.swing.JButton();
        lblIdbaixa = new javax.swing.JLabel();
        lblDiasDeAtraso = new javax.swing.JLabel();
        lblDiasAtrasoIf = new javax.swing.JLabel();
        lblMensalidade = new javax.swing.JLabel();

        jLabel2.setText("jLabel2");

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setTitle("Contas a Recber");
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

        txtAssocPesquisar.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtAssocPesquisarFocusLost(evt);
            }
        });
        txtAssocPesquisar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtAssocPesquisarMouseClicked(evt);
            }
        });
        txtAssocPesquisar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAssocPesquisarActionPerformed(evt);
            }
        });
        txtAssocPesquisar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtAssocPesquisarKeyReleased(evt);
            }
        });

        listPesquisarAssociaodo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        listPesquisarAssociaodo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listPesquisarAssociaodoMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                listPesquisarAssociaodoMousePressed(evt);
            }
        });

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/sistempresa/icones/pesquisar.png"))); // NOI18N

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

        jLabel7.setText("Num da Conta");

        txtIdRec.setEnabled(false);
        txtIdRec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdRecActionPerformed(evt);
            }
        });

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

        lblCnpjCpf.setText("Nosso Numero");

        jLabel15.setText("Pesquisar Associado");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Contas a receber"));

        tblAssociadosReceber = new javax.swing.JTable(){
            public boolean isCellEditable(int rowIndex, int colIndex){
                return false;
            }
        };
        tblAssociadosReceber.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Associado", "Emissão", "Vencimento", "Total", "Restante"
            }
        ));
        tblAssociadosReceber.getTableHeader().setReorderingAllowed(false);
        tblAssociadosReceber.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblAssociadosReceberMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblAssociadosReceber);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 724, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

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

        jLabel19.setText("Emisssão");

        jLabel20.setText("Valor Principal");

        jLabel21.setText("Valor Restante");

        txtValorRest.setText("0.00");
        txtValorRest.setToolTipText("");
        txtValorRest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtValorRestActionPerformed(evt);
            }
        });

        jLabel22.setText("Forma de Pag.");

        cboFormaDePag.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Boleto", "Cartão", "Dinheiro" }));

        txtNossoNum.setText("000000000000");
        txtNossoNum.setToolTipText("");

        txtDataEmissao.setEnabled(false);

        jLabel23.setText("Descrição");

        txtDescricaoRec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDescricaoRecActionPerformed(evt);
            }
        });

        jLabel24.setText("Vencimento");

        txtVencRec.setToolTipText("");
        txtVencRec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtVencRecActionPerformed(evt);
            }
        });

        labelStatusRec.setText("Aberto");
        labelStatusRec.setToolTipText("");

        jLabel25.setText("Parc.");

        jLabel26.setText("Banco");

        lblIdSinistro.setText("0");

        lblIdVeiculo.setText("0");

        lblIdAssociado.setText("0");

        jLabel3.setText("Associado do Sinistro");

        txtNomeAssocSinistro.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtNomeAssocSinistroFocusLost(evt);
            }
        });
        txtNomeAssocSinistro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNomeAssocSinistroActionPerformed(evt);
            }
        });
        txtNomeAssocSinistro.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtNomeAssocSinistroKeyReleased(evt);
            }
        });

        listAssocSinistro.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        listAssocSinistro.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                listAssocSinistroMousePressed(evt);
            }
        });

        lblStatusContaExcluida.setText("0");

        jLabel4.setText("Multa");

        txtMultaRec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMultaRecActionPerformed(evt);
            }
        });

        jLabel8.setText("Juros");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Conjuntos"));

        tblVeiculosAssoc = new javax.swing.JTable(){
            public boolean isCellEditable(int rowIndex, int colIndex){
                return false;
            }
        };
        tblVeiculosAssoc.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Id", "Placa", "Marca", "Modelo", "Cadastro"
            }
        ));
        jScrollPane2.setViewportView(tblVeiculosAssoc);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 696, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        txtValorPrinc.setText("0.00");
        txtValorPrinc.setToolTipText("");
        txtValorPrinc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtValorPrincActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Baixas"));

        tblBaixasRecebidas = new javax.swing.JTable(){
            public boolean isCellEditable(int rowIndex, int colIndex){
                return false;
            }
        };
        tblBaixasRecebidas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Id", "Valor da Baixa", "Data", "Forma de Pagamento", "Juros", "Multa"
            }
        ));
        tblBaixasRecebidas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblBaixasRecebidasMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tblBaixasRecebidas);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 696, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        painelBaixa.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Executar Baixa"));

        jLabel9.setText("Valor da Baixa");

        txtValorBaixaRec.setText("0,00");
        txtValorBaixaRec.setEnabled(false);
        txtValorBaixaRec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtValorBaixaRecActionPerformed(evt);
            }
        });

        jLabel10.setText("Data");

        txtDataBaixaRec.setText("  /  /");
        txtDataBaixaRec.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtDataBaixaRecFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtDataBaixaRecFocusLost(evt);
            }
        });
        txtDataBaixaRec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDataBaixaRecActionPerformed(evt);
            }
        });

        jLabel27.setText("Forma de Pag.");

        cboFormaDePagBaixa.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Boleto", "Cartão", "Dinheiro" }));

        jLabel11.setText("Multa");

        txtMultaBaixa.setText("0,0");
        txtMultaBaixa.setEnabled(false);
        txtMultaBaixa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMultaBaixaActionPerformed(evt);
            }
        });

        jLabel12.setText("Juros");

        txtJurosBaixa.setText("0,0");
        txtJurosBaixa.setEnabled(false);
        txtJurosBaixa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtJurosBaixaActionPerformed(evt);
            }
        });

        jLabel13.setText("Tipo de baixa");

        cboTipoDeBaixa.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Total", "Parcial" }));
        cboTipoDeBaixa.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboTipoDeBaixaItemStateChanged(evt);
            }
        });
        cboTipoDeBaixa.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cboTipoDeBaixaMouseClicked(evt);
            }
        });
        cboTipoDeBaixa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboTipoDeBaixaActionPerformed(evt);
            }
        });
        cboTipoDeBaixa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cboTipoDeBaixaKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                cboTipoDeBaixaKeyReleased(evt);
            }
        });

        btnBaixcarConta.setText("Baixar");
        btnBaixcarConta.setEnabled(false);
        btnBaixcarConta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBaixcarContaActionPerformed(evt);
            }
        });

        checkCobraJurosMulta.setText("Cobra Juros e Multa");
        checkCobraJurosMulta.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        checkCobraJurosMulta.setName(""); // NOI18N
        checkCobraJurosMulta.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkCobraJurosMultaItemStateChanged(evt);
            }
        });

        btnDesfazerBaixa.setText("Desfazer baixa");
        btnDesfazerBaixa.setEnabled(false);
        btnDesfazerBaixa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDesfazerBaixaActionPerformed(evt);
            }
        });

        lblIdbaixa.setText("id baixa");

        lblDiasAtrasoIf.setText("diasatraso");

        javax.swing.GroupLayout painelBaixaLayout = new javax.swing.GroupLayout(painelBaixa);
        painelBaixa.setLayout(painelBaixaLayout);
        painelBaixaLayout.setHorizontalGroup(
            painelBaixaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelBaixaLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(painelBaixaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelBaixaLayout.createSequentialGroup()
                        .addGroup(painelBaixaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelBaixaLayout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cboTipoDeBaixa, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(53, 53, 53)
                                .addComponent(checkCobraJurosMulta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(painelBaixaLayout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtValorBaixaRec, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtDataBaixaRec, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(painelBaixaLayout.createSequentialGroup()
                                .addComponent(jLabel27)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cboFormaDePagBaixa, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtMultaBaixa, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtJurosBaixa, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(20, Short.MAX_VALUE))
                    .addGroup(painelBaixaLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(painelBaixaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnDesfazerBaixa)
                            .addComponent(lblIdbaixa)
                            .addComponent(lblDiasAtrasoIf))
                        .addGap(129, 129, 129)
                        .addComponent(btnBaixcarConta, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(painelBaixaLayout.createSequentialGroup()
                        .addGap(251, 251, 251)
                        .addComponent(lblDiasDeAtraso)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        painelBaixaLayout.setVerticalGroup(
            painelBaixaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelBaixaLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(painelBaixaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(cboTipoDeBaixa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkCobraJurosMulta))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(painelBaixaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(txtValorBaixaRec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(txtDataBaixaRec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(painelBaixaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(cboFormaDePagBaixa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(txtMultaBaixa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(txtJurosBaixa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblDiasDeAtraso)
                .addGap(19, 19, 19)
                .addGroup(painelBaixaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnBaixcarConta)
                    .addComponent(btnDesfazerBaixa))
                .addGap(22, 22, 22)
                .addComponent(lblIdbaixa)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblDiasAtrasoIf)
                .addContainerGap(691, Short.MAX_VALUE))
        );

        lblMensalidade.setText("mens");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnRemover, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)
                        .addComponent(btnLimpar)
                        .addGap(98, 98, 98))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addGap(238, 238, 238)
                                        .addComponent(jLabel15)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(lblMensalidade)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtAssocPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(listPesquisarAssociaodo, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblIdVeiculo)
                                    .addComponent(lblStatusContaExcluida)
                                    .addComponent(txtIdEmpresa, javax.swing.GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
                                    .addComponent(lblIdAssociado, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblIdSinistro, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(30, 30, 30)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel23, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel24, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel22, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(listAssociado, javax.swing.GroupLayout.PREFERRED_SIZE, 538, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(18, 18, 18))
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(txtIdRec, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(lblCnpjCpf)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(txtNossoNum, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(jLabel19)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(txtDataEmissao)
                                                        .addGap(7, 7, 7)))
                                                .addComponent(labelStatusRec, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addComponent(txtNomeAssoc, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                                                .addComponent(txtDescricaoRec, javax.swing.GroupLayout.Alignment.LEADING))
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addGroup(layout.createSequentialGroup()
                                                    .addComponent(txtVencRec, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(jLabel20)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(txtValorPrinc, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGap(15, 15, 15)
                                                    .addComponent(jLabel21)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(txtValorRest, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGap(100, 100, 100))
                                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                    .addComponent(cboFormaDePag, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGap(17, 17, 17)
                                                    .addComponent(jLabel26)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(cboBanco, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(jLabel4)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(txtMultaRec, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(jLabel8)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(txtJurosRec, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(jLabel25)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(txtParc, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                    .addComponent(listAssocSinistro, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(txtNomeAssocSinistro, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)))))
                                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 752, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)))
                .addComponent(painelBaixa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(painelBaixa, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtAssocPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel15))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(listPesquisarAssociaodo)
                                    .addComponent(lblMensalidade))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel19)
                            .addComponent(txtDataEmissao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelStatusRec)
                            .addComponent(txtNossoNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCnpjCpf)
                            .addComponent(txtIdRec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7))
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(txtNomeAssoc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(listAssociado)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtDescricaoRec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel23))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtVencRec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel24)
                            .addComponent(jLabel20)
                            .addComponent(jLabel21)
                            .addComponent(txtValorRest, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtValorPrinc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel22)
                                .addComponent(cboFormaDePag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel26)
                                .addComponent(cboBanco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel4)
                                .addComponent(txtMultaRec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel8)
                                .addComponent(txtJurosRec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel25)
                                .addComponent(txtParc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtNomeAssocSinistro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(56, 56, 56)
                                .addComponent(lblIdSinistro)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblIdVeiculo)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblIdAssociado)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtIdEmpresa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblStatusContaExcluida))
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(listAssocSinistro)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(btnLimpar)
                                .addGap(29, 29, 29))
                            .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnRemover, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );

        btnImprimir.getAccessibleContext().setAccessibleDescription("Imprimir Documento de Adesão");
        btnPesquisar.getAccessibleContext().setAccessibleDescription("Pesquisar Por ID");

        setBounds(0, 0, 1288, 962);
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

    private void txtAssocPesquisarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAssocPesquisarActionPerformed
        listPesquisarAssociaodo.setVisible(false);
        EnterPesAssoc = 1;
    }//GEN-LAST:event_txtAssocPesquisarActionPerformed

    private void btnRemoverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoverActionPerformed
        remover();
    }//GEN-LAST:event_btnRemoverActionPerformed

    //metodo do tipo em tempo de execução conforme for informado a informação ele é acionado
    private void txtAssocPesquisarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtAssocPesquisarKeyReleased
        //Chamar o metodo pesquisa clientes 
        if (EnterPesAssoc == 0) {
            listaCampoPesquisaAssociado();
          

        } else {
            EnterPesAssoc = 0;
        }
        
         pesquisar_cliente();
    }//GEN-LAST:event_txtAssocPesquisarKeyReleased

    //Evento que sera usado para setar os campos da tabela clicando com o botão do mouse
    private void tblAssociadosReceberMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblAssociadosReceberMouseClicked
        // Chamando o metodo para setar os campos
        setar_campos();
    }//GEN-LAST:event_tblAssociadosReceberMouseClicked

    private void txtIdRecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdRecActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIdRecActionPerformed

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

    private void txtValorRestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtValorRestActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtValorRestActionPerformed

    private void txtAssocPesquisarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtAssocPesquisarMouseClicked
        pesquisar_cliente();
        mostrarPesquisaAssociado();
    }//GEN-LAST:event_txtAssocPesquisarMouseClicked

    private void txtDescricaoRecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDescricaoRecActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDescricaoRecActionPerformed

    private void txtVencRecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtVencRecActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtVencRecActionPerformed

    private void txtNomeAssocSinistroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNomeAssocSinistroActionPerformed
        listAssocSinistro.setVisible(false);
        EnterSin = 1;
    }//GEN-LAST:event_txtNomeAssocSinistroActionPerformed

    private void txtNomeAssocKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNomeAssocKeyReleased
        if (Enter == 0) {
            listaDePesquisaAssociado();
        } else {
            Enter = 0;
        }

    }//GEN-LAST:event_txtNomeAssocKeyReleased

    private void listAssociadoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listAssociadoMouseClicked
        mostrarPesquisaAssociado();
        listAssociado.setVisible(false);
        setarIdAssociado();
    }//GEN-LAST:event_listAssociadoMouseClicked

    private void txtNomeAssocSinistroKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNomeAssocSinistroKeyReleased
        if (EnterSin == 0) {
            listaDePesquisaAssociadoSinistro();
        } else {
            EnterSin = 0;
        }
    }//GEN-LAST:event_txtNomeAssocSinistroKeyReleased

    private void listAssocSinistroMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listAssocSinistroMousePressed
        mostrarPesquisaAssociadoSinistro();
        listAssocSinistro.setVisible(false);
    }//GEN-LAST:event_listAssocSinistroMousePressed

    private void txtMultaRecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMultaRecActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMultaRecActionPerformed

    private void txtValorPrincActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtValorPrincActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtValorPrincActionPerformed

    private void txtValorBaixaRecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtValorBaixaRecActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtValorBaixaRecActionPerformed

    private void txtDataBaixaRecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDataBaixaRecActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDataBaixaRecActionPerformed

    private void txtMultaBaixaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMultaBaixaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMultaBaixaActionPerformed

    private void btnBaixcarContaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBaixcarContaActionPerformed

        int confirma = JOptionPane.showConfirmDialog(null, "Confirme a baixa", "Atenção", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {
            baixarConta();

        }
    }//GEN-LAST:event_btnBaixcarContaActionPerformed

    private void txtDataBaixaRecFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtDataBaixaRecFocusGained
        txtDataBaixaRec.setFormatterFactory(new DefaultFormatterFactory(DataVencimento));
    }//GEN-LAST:event_txtDataBaixaRecFocusGained

    private void cboTipoDeBaixaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboTipoDeBaixaActionPerformed

    }//GEN-LAST:event_cboTipoDeBaixaActionPerformed

    private void txtJurosBaixaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtJurosBaixaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtJurosBaixaActionPerformed

    private void cboTipoDeBaixaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cboTipoDeBaixaKeyReleased

    }//GEN-LAST:event_cboTipoDeBaixaKeyReleased

    private void cboTipoDeBaixaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cboTipoDeBaixaKeyPressed

    }//GEN-LAST:event_cboTipoDeBaixaKeyPressed

    private void cboTipoDeBaixaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cboTipoDeBaixaMouseClicked

    }//GEN-LAST:event_cboTipoDeBaixaMouseClicked

    private void cboTipoDeBaixaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboTipoDeBaixaItemStateChanged
        String tipodebaixa = cboTipoDeBaixa.getSelectedItem().toString();

        if (tipodebaixa.equals("Parcial")) {
            txtValorBaixaRec.setEnabled(true);
            checkCobraJurosMulta.setSelected(false);

        } else {
            checkCobraJurosMulta.setSelected(false);
            checkCobraJurosMulta.setSelected(true);
            txtValorBaixaRec.setEnabled(false);
        }
    }//GEN-LAST:event_cboTipoDeBaixaItemStateChanged

    private void tblBaixasRecebidasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblBaixasRecebidasMouseClicked
        setar_campos_da_baixa();
    }//GEN-LAST:event_tblBaixasRecebidasMouseClicked

    private void btnDesfazerBaixaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDesfazerBaixaActionPerformed
        retornaValorBaixa();
    }//GEN-LAST:event_btnDesfazerBaixaActionPerformed

    private void checkCobraJurosMultaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_checkCobraJurosMultaItemStateChanged
        String tipodebaixa = cboTipoDeBaixa.getSelectedItem().toString();
        if (!checkCobraJurosMulta.isSelected()) {
            txtValorBaixaRec.setText(txtValorRest.getText());
            txtJurosBaixa.setText("0,0");
            txtMultaBaixa.setText("0,0");
        } else if (checkCobraJurosMulta.isSelected() & tipodebaixa.equals("Total")) {
            verificarAtraso();
        } else if (checkCobraJurosMulta.isSelected() & tipodebaixa.equals("Parcial")) {
            verificarAtrasoPagParcial();
        }
    }//GEN-LAST:event_checkCobraJurosMultaItemStateChanged

    private void txtDataBaixaRecFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtDataBaixaRecFocusLost
        java.util.Date data = new java.util.Date();
        DateFormat formatador = DateFormat.getDateInstance(DateFormat.MEDIUM);
        txtDataBaixaRec.setText(formatador.format(data));
    }//GEN-LAST:event_txtDataBaixaRecFocusLost

    private void txtNomeAssocSinistroFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNomeAssocSinistroFocusLost
        listAssocSinistro.setVisible(false);
        EnterSin = 0;
    }//GEN-LAST:event_txtNomeAssocSinistroFocusLost

    private void txtNomeAssocFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNomeAssocFocusLost
        listAssociado.setVisible(false);
        Enter = 0;
    }//GEN-LAST:event_txtNomeAssocFocusLost

    private void listPesquisarAssociaodoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listPesquisarAssociaodoMouseClicked
        mostrarAssociadoCampoPesquisa();
        listPesquisarAssociaodo.setVisible(false);
    }//GEN-LAST:event_listPesquisarAssociaodoMouseClicked

    private void txtAssocPesquisarFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtAssocPesquisarFocusLost
        listPesquisarAssociaodo.setVisible(false);
        EnterPesAssoc = 0;
    }//GEN-LAST:event_txtAssocPesquisarFocusLost

    private void listPesquisarAssociaodoMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listPesquisarAssociaodoMousePressed
        mostrarAssociadoCampoPesquisa();
        listPesquisarAssociaodo.setVisible(false);
    }//GEN-LAST:event_listPesquisarAssociaodoMousePressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionar;
    private javax.swing.JButton btnAlterar;
    private javax.swing.JButton btnBaixcarConta;
    private javax.swing.JButton btnDesfazerBaixa;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnLimpar;
    private javax.swing.JButton btnPesquisar;
    private javax.swing.JButton btnRemover;
    public static javax.swing.JComboBox<String> cboBanco;
    public static javax.swing.JComboBox<String> cboFormaDePag;
    public static javax.swing.JComboBox<String> cboFormaDePagBaixa;
    private javax.swing.JComboBox<String> cboTipoDeBaixa;
    private javax.swing.JCheckBox checkCobraJurosMulta;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel15;
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
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel labelStatusRec;
    private javax.swing.JLabel lblCnpjCpf;
    private javax.swing.JLabel lblDiasAtrasoIf;
    private javax.swing.JLabel lblDiasDeAtraso;
    private javax.swing.JLabel lblIdAssociado;
    private javax.swing.JLabel lblIdSinistro;
    private javax.swing.JLabel lblIdVeiculo;
    private javax.swing.JLabel lblIdbaixa;
    private javax.swing.JLabel lblMensalidade;
    private javax.swing.JLabel lblStatusContaExcluida;
    private javax.swing.JList<String> listAssocSinistro;
    private javax.swing.JList<String> listAssociado;
    private javax.swing.JList<String> listPesquisarAssociaodo;
    private javax.swing.JPanel painelBaixa;
    private javax.swing.JTable tblAssociadosReceber;
    private javax.swing.JTable tblBaixasRecebidas;
    private javax.swing.JTable tblVeiculosAssoc;
    private javax.swing.JTextField txtAssocPesquisar;
    private javax.swing.JFormattedTextField txtDataBaixaRec;
    private javax.swing.JFormattedTextField txtDataEmissao;
    private javax.swing.JTextField txtDescricaoRec;
    private javax.swing.JTextField txtIdEmpresa;
    public static javax.swing.JTextField txtIdRec;
    private javax.swing.JTextField txtJurosBaixa;
    private javax.swing.JTextField txtJurosRec;
    private javax.swing.JTextField txtMultaBaixa;
    private javax.swing.JTextField txtMultaRec;
    public static javax.swing.JTextField txtNomeAssoc;
    public static javax.swing.JTextField txtNomeAssocSinistro;
    private javax.swing.JTextField txtNossoNum;
    private javax.swing.JTextField txtParc;
    private javax.swing.JFormattedTextField txtValorBaixaRec;
    private javax.swing.JFormattedTextField txtValorPrinc;
    private javax.swing.JFormattedTextField txtValorRest;
    private javax.swing.JFormattedTextField txtVencRec;
    // End of variables declaration//GEN-END:variables
}
