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
import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
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

public class TelaAssociado extends javax.swing.JInternalFrame {

    //Usando a variavel de conexao do DAL
    Connection conexao = null;
    //Criando variaveis especiais para conexao com o banco 
    //Prepared Stetament e ResultSet são Frameworks do pacote java.sql
    // e servem para preparar e executar as instruções SQL
    PreparedStatement pst = null;
    ResultSet rs = null;

    private MaskFormatter CNPJMask, CPFMask, CEPMask, ValorParcUm, ValorMensal;
    private MaskFormatter Fone1Mask, Fone2Mask, Fone3Mask, DataVencimento, RenavanMask;
    int contereceberabertas;
    int diaParSin;
    int mesParSin;
    int anoParSin;
    LocalDate datavencimento;
    int numparcelas = 1;
    int totalconjuntostelaveic = 0;
    float valormensalantes = 0;
    int totalconjuntosantes = 0;

    /**
     * Creates new form TelaCliente
     */
    public TelaAssociado() {
        initComponents();
        conexao = ModuloConexao.conector();

        try {
            CNPJMask = new MaskFormatter("##.###.###/####-##");
            CPFMask = new MaskFormatter("###.###.###-##");
            CEPMask = new MaskFormatter("##.###-###");
            ValorParcUm = new MaskFormatter("#####");
            ValorMensal = new MaskFormatter("#####");
            Fone1Mask = new MaskFormatter("(##) # ####-####");
            Fone2Mask = new MaskFormatter("(##) # ####-####");
            Fone3Mask = new MaskFormatter("(##) # ####-####");
            DataVencimento = new MaskFormatter("##/##/####");

        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        txtAssocCep.setFormatterFactory(new DefaultFormatterFactory(CEPMask));
        txtAssocCnpjCpf.setFormatterFactory(new DefaultFormatterFactory(CNPJMask));
        //txtParcUmAssoc.setFormatterFactory(new DefaultFormatterFactory(ValorParcUm));
        //txtParcMensalAssoc.setFormatterFactory(new DefaultFormatterFactory(ValorMensal));
        txtVenc.setFormatterFactory(new DefaultFormatterFactory(DataVencimento));
        //txtAdesaoAssoc.setDocument(new LimitaCaracteres(60, LimitaCaracteres.TipoEntrada.NUMERODECIMAL));
        // txtValorMensalAssoc.setDocument(new LimitaCaracteres(60, LimitaCaracteres.TipoEntrada.NUMERODECIMAL));
        txtRenavanVeic.setDocument(new LimitaCaracteres(60, LimitaCaracteres.TipoEntrada.NUMEROINTEIRO));

        DecimalFormat decimal = new DecimalFormat("###,###,###.00");
        NumberFormatter numFormatter = new NumberFormatter(decimal);
        numFormatter.setFormat(decimal);
        numFormatter.setAllowsInvalid(false);
        DefaultFormatterFactory dfFactory = new DefaultFormatterFactory(numFormatter);

        txtValorMensalAssoc.setFormatterFactory(dfFactory);
        txtAdesaoAssoc.setFormatterFactory(dfFactory);

        setarIdEmpresa();
        popularComboBoxNomeBanco();

        //txtIdEmpresa.setVisible(false);
        String status = cboStatusAssoc.getSelectedItem().toString();

        if ("Ativo".equals(status)) {
            cboStatusAssoc.setEnabled(false);
        }

    }

    private void adicionar() {

        String dia = txtVenc.getText().substring(0, 2);
        String mes = txtVenc.getText().substring(3, 5);
        String ano = txtVenc.getText().substring(6);

        String datamysql = ano + "-" + mes + "-" + dia;

        String status = cboStatusAssoc.getSelectedItem().toString();
        int statusnum = 0;

        if ("Inativo".equals(status)) {
            statusnum = 1;
        }

        String sql = "insert into tbassociado(NOMEASSOC, NOMEFANT, REPRESENTANTE, UFASSOC, CIDADEASSOC, BAIRROASSOC, LOGRADOUROASSOC, COMPLEMENTOASSOC, CEPASSOC, FONEASSOC1, FONEASSOC2, FONEASSOC3, EMAILASSOC, CNPJCPFASSOC, TIPODOCUMENTO, idempr, valorparcum, valormensal, numconjunto, associadoexcluido, datavenc) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            pst = conexao.prepareStatement(sql);

            pst.setString(1, txtNomeAssoc.getText());
            pst.setString(2, txtAssocFantasia.getText());
            pst.setString(3, txtAssocRepresentante.getText());
            pst.setString(4, cboUf.getSelectedItem().toString());
            pst.setString(5, txtAssocCidade.getText());
            pst.setString(6, txtAssocBairro.getText());
            pst.setString(7, txtAssocLograd.getText());
            pst.setString(8, txtAssocCompl.getText());
            pst.setString(9, txtAssocCep.getText());
            pst.setString(10, txtAssocFone1.getText());
            pst.setString(11, txtAssocFone2.getText());
            pst.setString(12, txtAssocFone3.getText());
            pst.setString(13, txtAssocEmail.getText());
            pst.setString(14, txtAssocCnpjCpf.getText());
            pst.setString(15, cboCnpjCpf.getSelectedItem().toString());
            pst.setString(16, txtIdEmpresa.getText());
            pst.setString(17, txtAdesaoAssoc.getText().replace(".", "").replace(",", "."));
            pst.setString(18, txtValorMensalAssoc.getText().replace(".", "").replace(",", "."));
            pst.setString(19, cboNumConjAssoc.getSelectedItem().toString());
            pst.setString(20, String.valueOf(statusnum));
            pst.setString(21, datamysql);

            //Validação dos campos obrigatorios
            if ((txtNomeAssoc.getText().isEmpty()) || (txtAssocCnpjCpf.getText().isEmpty()) || (txtAssocRepresentante.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatorios.");

            } else if (cboNumConjAssoc.getSelectedItem().toString().equals("0")) {
                JOptionPane.showMessageDialog(null, "Informe a quantidade de conjunto.");
            } else if ((txtVenc.getText().equals("  /  /    ")) || (txtVenc.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null, "Informe o vencimento das mensalidades.");
            } else {
                int adicionado = pst.executeUpdate();

                buscaUltimoId();
                adicionarContasAReceberAdesao();

                for (numparcelas = numparcelas; numparcelas <= 12; numparcelas++) {

                    criarMensalidadesParaOAssociado();

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

                    txtVenc.setText(data);

                }
                //System.out.println(adicionado);
                if (adicionado > 0) {

                    JOptionPane.showMessageDialog(null, "Associado adicionado com sucesso juntamente com suas mensalidades no contas a receber.");

                    btnAdicionar.setEnabled(false);
                    btnAlterar.setEnabled(true);
                    btnRemover.setEnabled(true);
                    btnPesquisar.setEnabled(true);
                    btnImprimir.setEnabled(true);
                    btnAddConjuntos.setEnabled(true);
                    btnLimpar.setEnabled(true);

                    JOptionPane.showMessageDialog(null, "Adicione o(s) conjunto(s) do novo associado no campo ao lado.");

                    ativaCamposTxtAddVeic();
                    setarNumeroDeConj();

                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void adicionarContasAReceberAdesao() {

        String dia = txtVenc.getText().substring(0, 2);
        String mes = txtVenc.getText().substring(3, 5);
        String ano = txtVenc.getText().substring(6);

        String datamysql = ano + "-" + mes + "-" + dia;

        String adesao = "Adesãoo de Contrato.";

        String sql = "insert into tbcontasreceber(nossonumero, descricao, associado, valorprinc, valorrestante, parcelas, pago, formapag, datavenc, banco, idsin, idveic, idassoc,idempr, nomeassocsinistro, contaexcluida,mensalidade) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, "00000");
            pst.setString(2, adesao);
            pst.setString(3, txtNomeAssoc.getText());
            pst.setString(4, txtAdesaoAssoc.getText().replace(".", "").replace(",", "."));
            pst.setString(5, txtAdesaoAssoc.getText().replace(".", "").replace(",", "."));
            pst.setString(6, "1");
            pst.setString(7, "0");
            pst.setString(8, cboFormaDePag.getSelectedItem().toString());
            pst.setString(9, datamysql);
            pst.setString(10, cboBanco.getSelectedItem().toString());
            pst.setString(11, "0");
            pst.setString(12, "0");
            pst.setString(13, txtIdAssoc.getText());
            pst.setString(14, txtIdEmpresa.getText());
            pst.setString(15, "");
            pst.setString(16, "0");
            pst.setString(17, "0");

            int adicionado = pst.executeUpdate();

            if (adicionado > 0) {

                JOptionPane.showMessageDialog(null, "Adesão criada no contas as receber com sucesso.");

            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void criarMensalidadesParaOAssociado() {

        diaParSin = Integer.parseInt(txtVenc.getText().substring(0, 2));
        mesParSin = Integer.parseInt(txtVenc.getText().substring(3, 5));
        anoParSin = Integer.parseInt(txtVenc.getText().substring(6));

        datavencimento = LocalDate.of(anoParSin, mesParSin, diaParSin);

        String datamysql = String.valueOf(anoParSin) + "-" + String.valueOf(mesParSin) + "-" + String.valueOf(diaParSin);

        String adesao = "Mensalidade do Associado.";

        String sql = "insert into tbcontasreceber(nossonumero, descricao, associado, valorprinc, valorrestante, parcelas, pago, formapag, datavenc, banco, idsin, idveic, idassoc,idempr, nomeassocsinistro, contaexcluida, mensalidade) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, "00000");
            pst.setString(2, adesao);
            pst.setString(3, txtNomeAssoc.getText());
            pst.setString(4, txtValorMensalAssoc.getText().replace(".", "").replace(",", "."));
            pst.setString(5, txtValorMensalAssoc.getText().replace(".", "").replace(",", "."));
            pst.setString(6, String.valueOf(numparcelas));
            pst.setString(7, "0");
            pst.setString(8, cboFormaDePag.getSelectedItem().toString());
            pst.setString(9, datamysql);
            pst.setString(10, cboBanco.getSelectedItem().toString());
            pst.setString(11, "0");
            pst.setString(12, "0");
            pst.setString(13, txtIdAssoc.getText());
            pst.setString(14, txtIdEmpresa.getText());
            pst.setString(15, "");
            pst.setString(16, "0");
            pst.setString(17, "1");

            int adicionado = pst.executeUpdate();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    //PEsquisa cliente pelo nome
    private void pesquisar_cliente() {

        String sql = "select IDASSOC as ID, NOMEASSOC as Associado, NOMEFANT as Fantasia, REPRESENTANTE as Representante, CNPJCPFASSOC as CNPJCPF, EMAILASSOC AS Email from tbassociado where NOMEASSOC like ?";

        try {
            pst = conexao.prepareStatement(sql);
            //Passando o conteudo da caixa de pesquisa para o ?
            //atenção ao "%" - continuação da pesquisa sql
            pst.setString(1, txtAssocPesquisar.getText() + "%");
            rs = pst.executeQuery();
            // a linha abaixo usa a biblioteca rs2xml.jar para preencher a tabela 
            tblAssociados.setModel(DbUtils.resultSetToTableModel(rs));

            tblAssociados.getColumnModel().getColumn(0).setMaxWidth(50);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    //Metodo para setar os campos do formulario com o conteudo da tabela    
    public void setar_campos() {

        int setar = tblAssociados.getSelectedRow();

        txtIdAssoc.setText(tblAssociados.getModel().getValueAt(setar, 0).toString());

        String id_assoc = txtIdAssoc.getText();

        String sql = "select IDASSOC, date_format(DATACADASTASSOC,'%d/%m/%Y'), NOMEASSOC, NOMEFANT, REPRESENTANTE, UFASSOC, CIDADEASSOC, BAIRROASSOC, LOGRADOUROASSOC, COMPLEMENTOASSOC, CEPASSOC, FONEASSOC1, FONEASSOC2, FONEASSOC3, EMAILASSOC, TIPODOCUMENTO, CNPJCPFASSOC,Concat(   \n"
                + "               Replace  \n"
                + "                 (Replace  \n"
                + "                   (Replace  \n"
                + "                     (Format(valorparcum, 3), '.', '|'), ',', '.'), '|', ',')) AS parcum, Concat(   \n"
                + "               Replace  \n"
                + "                 (Replace  \n"
                + "                   (Replace  \n"
                + "                     (Format(valormensal, 3), '.', '|'), ',', '.'), '|', ',')) as mensalidade, numconjunto, associadoexcluido, date_format(datavenc,'%d/%m/%Y') from tbassociado where IDASSOC =" + id_assoc;

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {

                txtIdAssoc.setText(rs.getString(1));
                txtDataCadAssoc.setText(rs.getString(2));
                txtNomeAssoc.setText(rs.getString(3));
                txtAssocFantasia.setText(rs.getString(4));
                txtAssocRepresentante.setText(rs.getString(5));
                cboUf.setSelectedItem(rs.getString(6));
                txtAssocCidade.setText(rs.getString(7));
                txtAssocBairro.setText(rs.getString(8));
                txtAssocLograd.setText(rs.getString(9));
                txtAssocCompl.setText(rs.getString(10));
                txtAssocCep.setText(rs.getString(11));
                txtAssocFone1.setText(rs.getString(12));
                txtAssocFone2.setText(rs.getString(13));
                txtAssocFone3.setText(rs.getString(14));
                txtAssocEmail.setText(rs.getString(15));
                cboCnpjCpf.setSelectedItem(rs.getString(16));
                txtAssocCnpjCpf.setText(rs.getString(17));
                txtAdesaoAssoc.setText(rs.getString(18));
                txtValorMensalAssoc.setText(rs.getString(19));
                cboNumConjAssoc.setSelectedItem(rs.getString(20));
                lblStatusAssoc.setText(rs.getString(21));
                txtVenc.setText(rs.getString(22));

                setarNumeroDeConj();

                valormensalantes = Float.parseFloat(txtValorMensalAssoc.getText().replace(".", "").replace(",", "."));
                totalconjuntosantes = Integer.parseInt(cboNumConjAssoc.getSelectedItem().toString());

                String status = lblStatusAssoc.getText();

                if ("0".equals(status)) {
                    cboStatusAssoc.setSelectedItem("Ativo");
                } else {
                    cboStatusAssoc.setSelectedItem("Inativo");
                }

                String statusEnabled = cboStatusAssoc.getSelectedItem().toString();

                if ("Ativo".equals(statusEnabled)) {
                    cboStatusAssoc.setEnabled(false);
                } else {
                    cboStatusAssoc.setEnabled(true);
                }

                if ("0".equals(status)) {
                    btnAdicionar.setEnabled(true);
                    btnAlterar.setEnabled(true);
                    btnRemover.setEnabled(true);
                    btnPesquisar.setEnabled(true);
                    btnImprimir.setEnabled(true);
                    btnAddConjuntos.setEnabled(true);
                    btnLimpar.setEnabled(true);
                } else {
                    btnAdicionar.setEnabled(false);
                    btnAlterar.setEnabled(true);
                    btnRemover.setEnabled(false);
                    btnPesquisar.setEnabled(false);
                    btnImprimir.setEnabled(true);
                    btnAddConjuntos.setEnabled(false);
                    btnLimpar.setEnabled(true);
                }

                if (totalconjuntosantes < totalconjuntostelaveic) {
                    JOptionPane.showMessageDialog(null, "Associado com total de conjuntos no cadastro menor que o total de conjuntos cadastrados. Por favor clica no botão (Adicionar Veiculo) abaixo para inativar um dos conjuntos.");
                } else if (totalconjuntosantes > totalconjuntostelaveic) {
                    JOptionPane.showMessageDialog(null, "Associado com total de conjuntos no cadastro maior que o total de conjuntos cadastrados. Por favor clica no botão (Adicionar Veiculo) abaixo para adicionar novos conjuntos.");
                }

                int numeroConjTelaVeic = Integer.parseInt(txtNumConjVeic.getText());
                int numeroConjTelaAssoc = Integer.parseInt(cboNumConjAssoc.getSelectedItem().toString());

                if (numeroConjTelaVeic == numeroConjTelaAssoc) {
                    pesquisar_veiculo();
                    limparVeiculoTodos();
                    inativarCamposTxtAddVeic();

                } else if (numeroConjTelaVeic < numeroConjTelaAssoc) {
                    pesquisar_veiculo();
                    ativaCamposTxtAddVeic();
                    btnAdicionarVeic.setEnabled(true);

                } else if (numeroConjTelaVeic > numeroConjTelaAssoc) {
                    pesquisar_veiculo();
                    limparVeiculoTodos();
                    inativarCamposTxtAddVeic();

                }

                // setarNumeroDeConj();
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
        btnAdicionar.setEnabled(false);
    }

    private void consultar() {

        String id_assoc = JOptionPane.showInputDialog("Informe o ID do Associado?");

        String sql = "select IDASSOC, date_format(DATACADASTASSOC,'%d/%m/%Y'), NOMEASSOC, NOMEFANT, REPRESENTANTE, UFASSOC, CIDADEASSOC, BAIRROASSOC, LOGRADOUROASSOC, COMPLEMENTOASSOC, CEPASSOC, FONEASSOC1, FONEASSOC2, FONEASSOC3, EMAILASSOC, TIPODOCUMENTO, CNPJCPFASSOC,Concat(   \n"
                + "               Replace  \n"
                + "                 (Replace  \n"
                + "                   (Replace  \n"
                + "                     (Format(valorparcum, 3), '.', '|'), ',', '.'), '|', ',')) AS parcum, Concat(   \n"
                + "               Replace  \n"
                + "                 (Replace  \n"
                + "                   (Replace  \n"
                + "                     (Format(valormensal, 3), '.', '|'), ',', '.'), '|', ',')) as mensalidade, numconjunto, associadoexcluido, date_format(datavenc,'%d/%m/%Y') from tbassociado where IDASSOC =" + id_assoc;

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                txtIdAssoc.setText(rs.getString(1));
                txtDataCadAssoc.setText(rs.getString(2));
                txtNomeAssoc.setText(rs.getString(3));
                txtAssocFantasia.setText(rs.getString(4));
                txtAssocRepresentante.setText(rs.getString(5));
                cboUf.setSelectedItem(rs.getString(6));
                txtAssocCidade.setText(rs.getString(7));
                txtAssocBairro.setText(rs.getString(8));
                txtAssocLograd.setText(rs.getString(9));
                txtAssocCompl.setText(rs.getString(10));
                txtAssocCep.setText(rs.getString(11));
                txtAssocFone1.setText(rs.getString(12));
                txtAssocFone2.setText(rs.getString(13));
                txtAssocFone3.setText(rs.getString(14));
                txtAssocEmail.setText(rs.getString(15));
                cboCnpjCpf.setSelectedItem(rs.getString(16));
                txtAssocCnpjCpf.setText(rs.getString(17));
                txtAdesaoAssoc.setText(rs.getString(18));
                txtValorMensalAssoc.setText(rs.getString(19));
                cboNumConjAssoc.setSelectedItem(rs.getString(20));
                lblStatusAssoc.setText(rs.getString(21));
                txtVenc.setText(rs.getString(22));

                valormensalantes = Float.parseFloat(txtValorMensalAssoc.getText().replace(".", "").replace(",", "."));
                totalconjuntosantes = Integer.parseInt(cboNumConjAssoc.getSelectedItem().toString());

                String status = lblStatusAssoc.getText();

                if ("0".equals(status)) {
                    cboStatusAssoc.setSelectedItem("Ativo");
                } else {
                    cboStatusAssoc.setSelectedItem("Inativo");
                }

                String statusEnabled = cboStatusAssoc.getSelectedItem().toString();

                if ("Ativo".equals(statusEnabled)) {
                    cboStatusAssoc.setEnabled(false);
                } else {
                    cboStatusAssoc.setEnabled(true);
                }

                if ("0".equals(status)) {
                    btnAdicionar.setEnabled(true);
                    btnAlterar.setEnabled(true);
                    btnRemover.setEnabled(true);
                    btnPesquisar.setEnabled(true);
                    btnImprimir.setEnabled(true);
                    btnAddConjuntos.setEnabled(true);
                    btnLimpar.setEnabled(true);
                } else {
                    btnAdicionar.setEnabled(false);
                    btnAlterar.setEnabled(true);
                    btnRemover.setEnabled(false);
                    btnPesquisar.setEnabled(false);
                    btnImprimir.setEnabled(true);
                    btnAddConjuntos.setEnabled(false);
                    btnLimpar.setEnabled(true);
                }

                //setarNumeroDeConj();
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

    private void pesquisarPorCnpj() {

        String cnpj_assoc = txtAssocCnpjCpf.getText();

        String sql = "select IDASSOC, date_format(DATACADASTASSOC,'%d/%m/%Y'), NOMEASSOC, NOMEFANT, REPRESENTANTE, UFASSOC, CIDADEASSOC, BAIRROASSOC, LOGRADOUROASSOC, COMPLEMENTOASSOC, CEPASSOC, FONEASSOC1, FONEASSOC2, FONEASSOC3, EMAILASSOC, TIPODOCUMENTO, CNPJCPFASSOC,Concat(   \n"
                + "               Replace  \n"
                + "                 (Replace  \n"
                + "                   (Replace  \n"
                + "                     (Format(valorparcum, 3), '.', '|'), ',', '.'), '|', ',')) AS parcum, Concat(   \n"
                + "               Replace  \n"
                + "                 (Replace  \n"
                + "                   (Replace  \n"
                + "                     (Format(valormensal, 3), '.', '|'), ',', '.'), '|', ',')) as mensalidade, numconjunto, associadoexcluido, date_format(datavenc,'%d/%m/%Y') from tbassociado where CNPJCPFASSOC = '" + cnpj_assoc + "'";

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                txtIdAssoc.setText(rs.getString(1));
                txtDataCadAssoc.setText(rs.getString(2));
                txtNomeAssoc.setText(rs.getString(3));
                txtAssocFantasia.setText(rs.getString(4));
                txtAssocRepresentante.setText(rs.getString(5));
                cboUf.setSelectedItem(rs.getString(6));
                txtAssocCidade.setText(rs.getString(7));
                txtAssocBairro.setText(rs.getString(8));
                txtAssocLograd.setText(rs.getString(9));
                txtAssocCompl.setText(rs.getString(10));
                txtAssocCep.setText(rs.getString(11));
                txtAssocFone1.setText(rs.getString(12));
                txtAssocFone2.setText(rs.getString(13));
                txtAssocFone3.setText(rs.getString(14));
                txtAssocEmail.setText(rs.getString(15));
                cboCnpjCpf.setSelectedItem(rs.getString(16));
                txtAssocCnpjCpf.setText(rs.getString(17));
                txtAdesaoAssoc.setText(rs.getString(18));
                txtValorMensalAssoc.setText(rs.getString(19));
                cboNumConjAssoc.setSelectedItem(rs.getString(20));
                lblStatusAssoc.setText(rs.getString(21));
                txtVenc.setText(rs.getString(22));

                valormensalantes = Float.parseFloat(txtValorMensalAssoc.getText().replace(".", "").replace(",", "."));
                totalconjuntosantes = Integer.parseInt(cboNumConjAssoc.getSelectedItem().toString());

                String status = lblStatusAssoc.getText();

                if ("0".equals(status)) {
                    cboStatusAssoc.setSelectedItem("Ativo");
                } else {
                    cboStatusAssoc.setSelectedItem("Inativo");
                }

                String statusEnabled = cboStatusAssoc.getSelectedItem().toString();

                if ("Ativo".equals(statusEnabled)) {
                    cboStatusAssoc.setEnabled(false);
                } else {
                    cboStatusAssoc.setEnabled(true);
                }

                if ("0".equals(status)) {
                    btnAdicionar.setEnabled(true);
                    btnAlterar.setEnabled(true);
                    btnRemover.setEnabled(true);
                    btnPesquisar.setEnabled(true);
                    btnImprimir.setEnabled(true);
                    btnAddConjuntos.setEnabled(true);
                    btnLimpar.setEnabled(true);
                } else {
                    btnAdicionar.setEnabled(false);
                    btnAlterar.setEnabled(true);
                    btnRemover.setEnabled(false);
                    btnPesquisar.setEnabled(false);
                    btnImprimir.setEnabled(true);
                    btnAddConjuntos.setEnabled(false);
                    btnLimpar.setEnabled(true);
                }

                setarNumeroDeConj();

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

    private void consultarCNPJCPF() {

        String cnpjcpfassoc = txtAssocCnpjCpf.getText();

        String sql = "select * from tbassociado where CNPJCPFASSOC = " + "'" + cnpjcpfassoc + "'";

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {

                pesquisarPorCnpj();

                String status = lblStatusAssoc.getText();

                if ("0".equals(status)) {
                    JOptionPane.showMessageDialog(null, "CNPJ / CPF ja cadastrado.");
                } else {
                    JOptionPane.showMessageDialog(null, "Associado Inativo.");
                }

            } else {
                buscarCNPJ();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void alterarAssociado() {

        /*String cepMasc = txtAssocCep.getText();
        String cep = cepMasc.replaceAll("\\D", "");

        String cnpjMasc = txtAssocCnpjCpf.getText();
        String cnpj = cnpjMasc.replaceAll("\\D", "");
        
        String valorParcUmMasc = txtParcUmAssoc.getText();
        String parcum = valorParcUmMasc.replaceAll("\\D", "");

        String valorParcMensal = txtParcMensalAssoc.getText();
        String mensalidade = valorParcMensal.replaceAll("\\D", "");*/
        String dia = txtVenc.getText().substring(0, 2);
        String mes = txtVenc.getText().substring(3, 5);
        String ano = txtVenc.getText().substring(6);

        String datamysql = ano + "-" + mes + "-" + dia;

        String status = cboStatusAssoc.getSelectedItem().toString();
        int statusnum = 0;

        if ("Inativo".equals(status)) {
            statusnum = 1;
        }

        String sql = "update tbassociado set NOMEASSOC=?, NOMEFANT=?, REPRESENTANTE=?, UFASSOC=?, CIDADEASSOC=?, BAIRROASSOC=?, LOGRADOUROASSOC=?, COMPLEMENTOASSOC=?, CEPASSOC=?, FONEASSOC1=?, FONEASSOC2=?, FONEASSOC3=?, EMAILASSOC=?, TIPODOCUMENTO=?, CNPJCPFASSOC=?, valorparcum=?,valormensal=?,numconjunto=?, associadoexcluido=?, datavenc=? where IDASSOC = ?";

        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, txtNomeAssoc.getText());
            pst.setString(2, txtAssocFantasia.getText());
            pst.setString(3, txtAssocRepresentante.getText());
            pst.setString(4, cboUf.getSelectedItem().toString());
            pst.setString(5, txtAssocCidade.getText());
            pst.setString(6, txtAssocBairro.getText());
            pst.setString(7, txtAssocLograd.getText());
            pst.setString(8, txtAssocCompl.getText());
            pst.setString(9, txtAssocCep.getText());
            pst.setString(10, txtAssocFone1.getText());
            pst.setString(11, txtAssocFone2.getText());
            pst.setString(12, txtAssocFone3.getText());
            pst.setString(13, txtAssocEmail.getText());
            pst.setString(14, cboCnpjCpf.getSelectedItem().toString());
            pst.setString(15, txtAssocCnpjCpf.getText());
            pst.setString(16, txtAdesaoAssoc.getText().replace(".", "").replace(",", "."));
            pst.setString(17, txtValorMensalAssoc.getText().replace(".", "").replace(",", "."));
            pst.setString(18, cboNumConjAssoc.getSelectedItem().toString());
            pst.setString(19, String.valueOf(statusnum));
            pst.setString(20, datamysql);
            pst.setString(21, txtIdAssoc.getText());

            System.out.println(statusnum);

            //Validação dos campos obrigatorios
            if ((txtNomeAssoc.getText().isEmpty()) || (txtAssocLograd.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatorios.");

            } else if (cboNumConjAssoc.getSelectedItem().toString().equals("0")) {
                JOptionPane.showMessageDialog(null, "Informe a quantidade de conjunto.");
            } else if ((txtVenc.getText().equals("  /  /    ")) || (txtVenc.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null, "Informe o vencimento das mensalidades.");
            } else {

                String statusnumerico = lblStatusAssoc.getText();
                int totalconjuntonovo = Integer.parseInt(cboNumConjAssoc.getSelectedItem().toString());
                float valormensalnovo = Float.parseFloat(txtValorMensalAssoc.getText().replace(".", "").replace(",", "."));

                //int adicionado = pst.executeUpdate();
                if ("1".equals(statusnumerico) & "Ativo".equals(status)) {

                    int adicionado = pst.executeUpdate();

                    adicionarContasAReceberAdesao();
                    reativarcontasareceberpagas();
                    reativaVeiculo();

                    for (numparcelas = numparcelas; numparcelas <= 12; numparcelas++) {

                        criarMensalidadesParaOAssociado();

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

                        txtVenc.setText(data);

                    }

                    if (adicionado > 0) {

                        JOptionPane.showMessageDialog(null, "Mensalidades criada no contas as receber com sucesso.");

                        limpar();

                        btnAdicionar.setEnabled(false);
                        btnAlterar.setEnabled(true);
                        btnRemover.setEnabled(true);
                        btnPesquisar.setEnabled(true);
                        btnImprimir.setEnabled(true);

                    }

                } else if ("0".equals(statusnumerico)) {

                    if (totalconjuntonovo != totalconjuntosantes) {

                        if (valormensalantes == valormensalnovo) {
                            int confirma = JOptionPane.showConfirmDialog(null, "Quantidade de conjuntos foi alterada ,no entanto valor da mensalidade continuar igual, deseja alterar o valor da mensalidade?", "Atenção", JOptionPane.YES_NO_OPTION);
                            if (confirma == JOptionPane.YES_OPTION) {

                                JOptionPane.showMessageDialog(null, "Alterações canceladas por favor altera o valor da mensalidade.");
                                txtValorMensalAssoc.requestFocus();

                            } else if (confirma == JOptionPane.NO_OPTION) {

                                int adicionado = pst.executeUpdate();

                                if (adicionado > 0) {

                                    if (totalconjuntonovo < totalconjuntosantes) {
                                        JOptionPane.showMessageDialog(null, "Dados do associado alterado com sucesso, por for favor inativar o conjunto removido clicando no botão abaixo (Adicionar Conjunto).");
                                    } else if (totalconjuntonovo > totalconjuntosantes) {
                                        JOptionPane.showMessageDialog(null, "Dados do associado alterado com sucesso, por for favor adicionar o novo conjunto clicando no botão abaixo (Adicionar Conjunto).");
                                    }

                                    //limpar();
                                    btnAdicionar.setEnabled(false);
                                    btnAlterar.setEnabled(true);
                                    btnRemover.setEnabled(true);
                                    btnPesquisar.setEnabled(true);
                                    btnImprimir.setEnabled(true);

                                }

                            }
                        } else if (valormensalantes != valormensalnovo) {

                            int adicionado2 = pst.executeUpdate();

                            alterarValorMensalidade();

                            if (adicionado2 > 0) {

                                JOptionPane.showMessageDialog(null, "Dado(s) do associado alterado(s) com sucesso e valores das mensalidades em aberto foram aterados conforme o novo valor da mensalidade.");

                                //limpar();
                                btnAdicionar.setEnabled(false);
                                btnAlterar.setEnabled(true);
                                btnRemover.setEnabled(true);
                                btnPesquisar.setEnabled(true);
                                btnImprimir.setEnabled(true);
                            }

                        }

                    } else if (totalconjuntonovo == totalconjuntosantes) {

                        if (valormensalantes == valormensalnovo) {

                            int adicionado = pst.executeUpdate();

                            if (adicionado > 0) {

                                JOptionPane.showMessageDialog(null, "Dados do associado alterado(s) com sucesso.");

                                //limpar();
                                btnAdicionar.setEnabled(false);
                                btnAlterar.setEnabled(true);
                                btnRemover.setEnabled(true);
                                btnPesquisar.setEnabled(true);
                                btnImprimir.setEnabled(true);

                            }

                        } else if (valormensalantes != valormensalnovo) {

                            int adicionado2 = pst.executeUpdate();

                            alterarValorMensalidade();

                            if (adicionado2 > 0) {

                                JOptionPane.showMessageDialog(null, "Dado(s) do associado alterado(s) com sucesso e valores das mensalidades em aberto foram aterados conforme o novo valor da mensalidade.");

                                //limpar();
                                btnAdicionar.setEnabled(false);
                                btnAlterar.setEnabled(true);
                                btnRemover.setEnabled(true);
                                btnPesquisar.setEnabled(true);
                                btnImprimir.setEnabled(true);
                            }

                        }

                    }

                }

            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void alterarValorMensalidade() {

        String sql = "update tbcontasreceber set valorprinc=?, valorrestante=? where IDASSOC = ? and mensalidade = 1;";

        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, txtValorMensalAssoc.getText().replace(".", "").replace(",", "."));
            pst.setString(2, txtValorMensalAssoc.getText().replace(".", "").replace(",", "."));
            pst.setString(3, txtIdAssoc.getText());

            //Validação dos campos obrigatorios
            int adicionado = pst.executeUpdate();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void reativaVeiculo() {

        String sql = "update tbveiculo set veiculoexcluido = 0 where idassoc=?";

        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, txtIdAssoc.getText());
            int apagado = pst.executeUpdate();

            if (apagado > 0) {
                JOptionPane.showMessageDialog(null, "Conjuntos reativados com sucesso.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void reativarcontasareceberpagas() {

        String sql = "update tbcontasreceber set contaexcluida = 0 where pago = 1 and IDASSOC = ?;";

        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, txtIdAssoc.getText());
            int apagado = pst.executeUpdate();

            if (apagado > 0) {
                JOptionPane.showMessageDialog(null, "Contas pagas do associado reativadas com sucesso");

            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void buscaDividasEmAberto() {

        String sql = "select COUNT(*) from tbcontasreceber where pago = 0;";

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                contereceberabertas = Integer.parseInt(rs.getString(1));

            }
        } catch (Exception e2) {
            JOptionPane.showMessageDialog(null, e2);
        }
    }

    private void remover() {

        buscaDividasEmAberto();

        if (contereceberabertas > 0) {
            int confirma = JOptionPane.showConfirmDialog(null, "Associados com contas a receber em aberto, deseja excluir este associado e suas contas?", "Atenção", JOptionPane.YES_NO_OPTION);

            if (confirma == JOptionPane.YES_OPTION) {
                String sql = "update tbassociado set associadoexcluido = 1 where IDASSOC = ?";
                try {
                    pst = conexao.prepareStatement(sql);
                    pst.setString(1, txtIdAssoc.getText());
                    int apagado = pst.executeUpdate();

                    if (apagado > 0) {

                        intaivarcontasareceberpagas();
                        removercontasarecebeabertas();
                        inativaVeiculo();

                        JOptionPane.showMessageDialog(null, "Associado inativado com sucesso");

                    }

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, e);
                }
            }
        } else {
            String sql = "update tbassociado set associadoexcluido = 1 where IDASSOC = ?";
            try {
                pst = conexao.prepareStatement(sql);
                pst.setString(1, txtIdAssoc.getText());
                int apagado = pst.executeUpdate();

                if (apagado > 0) {

                    intaivarcontasareceberpagas();
                    removercontasarecebeabertas();
                    inativaVeiculo();

                    JOptionPane.showMessageDialog(null, "Associado inativado com sucesso");

                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }

        }
    }

    private void intaivarcontasareceberpagas() {

        String sql = "update tbcontasreceber set contaexcluida = 1 where pago = 1 and IDASSOC = ?;";

        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, txtIdAssoc.getText());
            int apagado = pst.executeUpdate();

            if (apagado > 0) {
                JOptionPane.showMessageDialog(null, "Contas pagas do associado inativadas com sucesso");

            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void removercontasarecebeabertas() {

        String sql = "delete from tbcontasreceber where pago = 0 and IDASSOC = ?;";

        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, txtIdAssoc.getText());
            int apagado = pst.executeUpdate();

            if (apagado > 0) {
                JOptionPane.showMessageDialog(null, "Contas abertas do associado excluídas com sucesso");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void inativaVeiculo() {

        String sql = "update tbveiculo set veiculoexcluido = 1 where idassoc=?";

        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, txtIdAssoc.getText());
            int apagado = pst.executeUpdate();

            if (apagado > 0) {
                JOptionPane.showMessageDialog(null, "Veiculos inativados com sucesso");

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

    private void buscarCep() {

        String logradouro = "";
        String tipologradouro = "";
        String resultado = null;
        String cep = txtAssocCep.getText();

        try {
            URL url = new URL("http://cep.republicavirtual.com.br/web_cep.php?cep=" + cep + "&formato=xml");
            SAXReader xml = new SAXReader();
            Document documento = xml.read(url);
            Element root = documento.getRootElement();

            for (Iterator<Element> it = root.elementIterator(); it.hasNext();) {
                Element element = it.next();

                if (element.getQualifiedName().equals("bairro")) {
                    txtAssocBairro.setText(element.getText());
                }
                if (element.getQualifiedName().equals("cidade")) {
                    txtAssocCidade.setText(element.getText());
                }
                if (element.getQualifiedName().equals("uf")) {
                    cboUf.setSelectedItem(element.getText());
                }
                if (element.getQualifiedName().equals("tipo_logradouro")) {
                    tipologradouro = element.getText();
                }
                if (element.getQualifiedName().equals("logradouro")) {
                    logradouro = element.getText();
                }
                if (element.getQualifiedName().equals("resultado")) {
                    resultado = element.getText();
                    if (resultado.equals("1")) {

                    } else {
                        JOptionPane.showMessageDialog(null, "CEP não encontrado");
                    }
                }
            }

            //Setar o campo endereco
            txtAssocLograd.setText(tipologradouro + " " + logradouro);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void buscarCNPJ() throws MalformedURLException {

        String cnpjMasc = txtAssocCnpjCpf.getText();
        String cnpj = cnpjMasc.replaceAll("\\D", "");

        URL url = new URL("https://receitaws.com.br/v1/cnpj/" + cnpj);

        JSONParser parser = new JSONParser();
        JSONObject jSONObject;
        String text = "";
        String status = "";
        String logradouro = "";
        String numero = "";

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line = null;

            // Read Server Response
            while ((line = reader.readLine()) != null) {
                // Append server response in string
                sb.append(line + "\n");
            }

            text = sb.toString();

            try {

                jSONObject = (JSONObject) parser.parse(text);

                txtNomeAssoc.setText((String) jSONObject.get("nome"));
                txtAssocFantasia.setText((String) jSONObject.get("fantasia"));
                txtAssocCep.setText((String) jSONObject.get("cep"));
                logradouro = (String) jSONObject.get("logradouro");
                numero = (String) jSONObject.get("numero");
                txtAssocLograd.setText(logradouro + " Nº " + numero);
                txtAssocCompl.setText((String) jSONObject.get("complemento"));
                txtAssocBairro.setText((String) jSONObject.get("bairro"));
                txtAssocCidade.setText((String) jSONObject.get("municipio"));
                cboUf.setSelectedItem((String) jSONObject.get("uf"));
                txtAssocFone1.setText((String) jSONObject.get("telefone"));
                txtAssocEmail.setText((String) jSONObject.get("email"));
                //System.out.println((String) jSONObject.get("bairro"));
                status = (String) jSONObject.get("status");
                System.out.println(status);

                if (status.equals("ERROR")) {
                    JOptionPane.showMessageDialog(null, "CNPJ inválido");
                }

            } catch (org.json.simple.parser.ParseException e) {
                JOptionPane.showMessageDialog(null, "Erro para converter");
            }

        } catch (IOException e2) {
            JOptionPane.showMessageDialog(null, "Erro ao consulta CNPJ");
        }

    }

    public void limpar() {
        txtIdAssoc.setText(null);
        txtNomeAssoc.setText(null);
        txtAssocFantasia.setText(null);
        txtAssocRepresentante.setText(null);
        cboUf.setSelectedIndex(0);
        txtAssocCidade.setText(null);
        txtAssocBairro.setText(null);
        txtAssocLograd.setText(null);
        txtAssocCompl.setText(null);
        txtAssocCep.setText(null);
        txtAssocFone1.setText(null);
        txtAssocFone2.setText(null);
        txtAssocFone3.setText(null);
        txtAssocEmail.setText(null);
        txtAssocCnpjCpf.setText(null);
        cboCnpjCpf.setSelectedIndex(0);
        cboNumConjAssoc.setSelectedIndex(0);
        txtAdesaoAssoc.setText(null);
        txtValorMensalAssoc.setText(null);
        txtVenc.setFormatterFactory(new DefaultFormatterFactory(DataVencimento));

        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(false);
        btnRemover.setEnabled(false);
        btnAddConjuntos.setEnabled(false);
        btnLimpar.setEnabled(false);

        //limpaTabela
        ((DefaultTableModel) tblVeiculos.getModel()).setRowCount(0);

        limparVeiculoTodos();
        inativarCamposTxtAddVeic();

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

    private void imprimirdoc_adesao() {

        int confirma = JOptionPane.showConfirmDialog(null, "Imprimir documento de adesão?", "Atenção", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {
            //Imprimir relatorio com o framework JasperReport
            try {
                // usando a classe HashMap para criar um filtro
                HashMap filtro = new HashMap();
                filtro.put("idassoc", Integer.parseInt(txtIdAssoc.getText()));
                JasperPrint print = JasperFillManager.fillReport(getClass().getResourceAsStream("/impressao/termodeadesao.jasper"), filtro, conexao);
                //a linha abaixo apresenta o relatorio através da classe JasperViewer
                JasperViewer.viewReport(print, false);

            } catch (JRException e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    private void buscaUltimoId() {

        String sql = "select IDASSOC from tbassociado order by IDASSOC desc limit 1;";

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                txtIdAssoc.setText(rs.getString(1));

            }
        } catch (Exception e2) {
            JOptionPane.showMessageDialog(null, e2);
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

    public void setarNumeroDeConj() {

        String id_assoc1 = txtIdAssoc.getText();
        String id_empr = txtIdEmpresa.getText();
        String statusveiculo = String.valueOf(0);

        String sql = "SELECT COUNT(*) FROM  tbveiculo where idassoc = " + id_assoc1 + " and idempr = " + id_empr + " and veiculoexcluido = 0";

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                totalconjuntostelaveic = Integer.parseInt(rs.getString(1));
                txtNumConjVeic.setText(rs.getString(1));

            }
        } catch (SQLException e2) {
            JOptionPane.showMessageDialog(null, e2);
        }

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //ADICIONAR VEÍCULOS >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    private void adicionarVeiculo() {

        int numeroConjTelaVeic = Integer.parseInt(txtNumConjVeic.getText()) + 1;
        int numeroConjTelaAssoc = Integer.parseInt(cboNumConjAssoc.getSelectedItem().toString());

        if (numeroConjTelaVeic < numeroConjTelaAssoc) {

            String sql = "insert into tbveiculo(nomepropr, marca, modelo, ano, placa, renavan, "
                    + "idassoc, numcarretaveic, numdollyveic, idempr, veiculoexcluido) values(?,?,?,?,?,?,?,?,?,?,?)";

            try {
                pst = conexao.prepareStatement(sql);

                pst.setString(1, txtNomeAssoc.getText());
                pst.setString(2, txtMarcaVeic.getText());
                pst.setString(3, txtModeloVeic.getText());
                pst.setString(4, txtAnoVeic.getText());
                pst.setString(5, txtPlacaVeic.getText());
                pst.setString(6, txtRenavanVeic.getText());
                pst.setString(7, txtIdAssoc.getText());
                pst.setString(8, cboNumCarretaVeic.getSelectedItem().toString());
                pst.setString(9, cboNumDollyVeic.getSelectedItem().toString());
                pst.setString(10, txtIdEmpresa.getText());
                pst.setString(11, "0");

                if ((txtNomeAssoc.getText().isEmpty()) || (txtRenavanVeic.getText().isEmpty()) || (txtPlacaVeic.getText().isEmpty()) || (cboNumCarretaVeic.getSelectedItem().toString().isEmpty()) || (cboNumDollyVeic.getSelectedItem().toString().isEmpty())) {
                    JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatorios.");

                } else {
                    int adicionado = pst.executeUpdate();

                    //System.out.println(adicionado);
                    if (adicionado > 0) {

                        JOptionPane.showMessageDialog(null, "Veiculo adicionado com sucesso, , adicione o proxímo veiculo");

                        setarNumeroDeConj();

                        limparVeiculo();

                    }
                }
                //for (int numconjuntos = Integer.parseInt(txtNumConjVeic.getText()); numconjuntos > 1; numconjuntos--) {
                //Validação dos campos obrigatorios
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        } else if (numeroConjTelaVeic == numeroConjTelaAssoc) {

            String sql = "insert into tbveiculo(nomepropr, marca, modelo, ano, placa, renavan, "
                    + "idassoc, numcarretaveic, numdollyveic, idempr, veiculoexcluido) values(?,?,?,?,?,?,?,?,?,?,?)";

            try {
                pst = conexao.prepareStatement(sql);

                pst.setString(1, txtNomeAssoc.getText());
                pst.setString(2, txtMarcaVeic.getText());
                pst.setString(3, txtModeloVeic.getText());
                pst.setString(4, txtAnoVeic.getText());
                pst.setString(5, txtPlacaVeic.getText());
                pst.setString(6, txtRenavanVeic.getText());
                pst.setString(7, txtIdAssoc.getText());
                pst.setString(8, cboNumCarretaVeic.getSelectedItem().toString());
                pst.setString(9, cboNumDollyVeic.getSelectedItem().toString());
                pst.setString(10, txtIdEmpresa.getText());
                pst.setString(11, "0");

                if ((txtNomeAssoc.getText().isEmpty()) || (txtRenavanVeic.getText().isEmpty()) || (txtPlacaVeic.getText().isEmpty())) {
                    JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatorios.");

                } else {
                    int adicionado = pst.executeUpdate();

                    //System.out.println(adicionado);
                    if (adicionado > 0) {

                        JOptionPane.showMessageDialog(null, "Veiculo(s) adicionado(s) com sucesso");

                        setarNumeroDeConj();

                        limparVeiculo();
                        limpar();

                        inativarCamposTxtAddVeic();

                    }
                }

                //for (int numconjuntos = Integer.parseInt(txtNumConjVeic.getText()); numconjuntos > 1; numconjuntos--) {
                //Validação dos campos obrigatorios
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }

        }

    }

    private void pesquisar_veiculo() {

        String sql = "select idveic as ID, nomepropr as Associado, marca as Marca, modelo as Modelo, ano as Ano, placa AS Placa, renavan AS Renavan from tbveiculo where idassoc = ?";

        try {
            pst = conexao.prepareStatement(sql);
            //Passando o conteudo da caixa de pesquisa para o ?
            //atenção ao "%" - continuação da pesquisa sql
            pst.setString(1, txtIdAssoc.getText());
            rs = pst.executeQuery();
            // a linha abaixo usa a biblioteca rs2xml.jar para preencher a tabela 
            tblVeiculos.setModel(DbUtils.resultSetToTableModel(rs));

            tblVeiculos.getColumnModel().getColumn(0).setMaxWidth(50);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    public void setar_camposVeiculos() {

        int setar = tblVeiculos.getSelectedRow();

        txtIdVeic.setText(tblVeiculos.getModel().getValueAt(setar, 0).toString());

        String id_veic = txtIdVeic.getText();

        String sql = "select idveic, date_format(data_cadveic,'%d/%m/%Y'), nomepropr, marca, modelo, ano, placa, renavan, idassoc, numcarretaveic, numdollyveic, idempr, veiculoexcluido from tbveiculo where idveic = " + id_veic;

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {

                txtIdVeic.setText(rs.getString(1));
                txtDataCadVeic.setText(rs.getString(2));
                txtNomeAssoc.setText(rs.getString(3));
                txtMarcaVeic.setText(rs.getString(4));
                txtModeloVeic.setText(rs.getString(5));
                txtAnoVeic.setText(rs.getString(6));
                txtPlacaVeic.setText(rs.getString(7));
                txtRenavanVeic.setText(rs.getString(8));
                txtIdAssoc.setText(rs.getString(9));
                cboNumCarretaVeic.setSelectedItem(rs.getString(10));
                cboNumDollyVeic.setSelectedItem(rs.getString(11));
                txtIdEmpresa.setText(rs.getString(12));

                lblStatusVeic.setText(rs.getString(13));

                String status = lblStatusVeic.getText();

                if ("0".equals(status)) {
                    cboStatusVeic.setSelectedItem("Ativo");
                } else {
                    cboStatusVeic.setSelectedItem("Inativo");
                }

                int numeroConjTelaVeic = Integer.parseInt(txtNumConjVeic.getText());
                int numeroConjTelaAssoc = Integer.parseInt(cboNumConjAssoc.getSelectedItem().toString());

                if (numeroConjTelaAssoc == numeroConjTelaVeic) {
                    btnAdicionarVeic.setEnabled(false);
                    btnAlterarVeic.setEnabled(true);
                    btnRemoverVeic.setEnabled(true);
                    btnImprimirVeic.setEnabled(true);
                } else if (numeroConjTelaAssoc > numeroConjTelaVeic) {
                    btnAdicionarVeic.setEnabled(true);
                    btnAlterarVeic.setEnabled(false);
                    btnRemoverVeic.setEnabled(false);
                    btnImprimirVeic.setEnabled(false);
                } else if (numeroConjTelaAssoc < numeroConjTelaVeic) {
                    btnAdicionarVeic.setEnabled(false);
                    btnAlterarVeic.setEnabled(true);
                    btnRemoverVeic.setEnabled(true);
                    btnImprimirVeic.setEnabled(true);
                }

                ativaCamposTxtAddVeic();

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
        btnAdicionarVeic.setEnabled(false);
        btnLimparVeic.setEnabled(true);
    }

    private void consultarVeiculos() {

        String id_veic = JOptionPane.showInputDialog("Informe o ID do Veiculo?");

        String sql = "select idveic, date_format(data_cadveic,'%d/%m/%Y'), nomepropr, marca, modelo, ano, placa, renavan, idassoc, numcarretaveic, numdollyveic, idempr from tbveiculo where idveic = " + id_veic;

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {

                txtIdVeic.setText(rs.getString(1));
                txtDataCadVeic.setText(rs.getString(2));
                txtNomeAssoc.setText(rs.getString(3));
                txtMarcaVeic.setText(rs.getString(4));
                txtModeloVeic.setText(rs.getString(5));
                txtAnoVeic.setText(rs.getString(6));
                txtPlacaVeic.setText(rs.getString(7));
                txtRenavanVeic.setText(rs.getString(8));
                txtIdAssoc.setText(rs.getString(9));
                cboNumCarretaVeic.setSelectedItem(rs.getString(10));
                cboNumDollyVeic.setSelectedItem(rs.getString(11));
                txtIdEmpresa.setText(rs.getString(12));

                lblStatusVeic.setText(rs.getString(13));

                String status = lblStatusVeic.getText();

                if ("0".equals(status)) {
                    cboStatusVeic.setSelectedItem("Ativo");
                } else {
                    cboStatusVeic.setSelectedItem("Inativo");
                }

                btnAdicionar.setEnabled(false);
                btnAlterar.setEnabled(true);
                btnRemover.setEnabled(true);
                btnPesquisar.setEnabled(true);
                btnImprimir.setEnabled(true);

            } else {
                JOptionPane.showMessageDialog(null, "Veiculo não cadastrado.");
                limpar();

            }
        } catch (SQLSyntaxErrorException e) {
            JOptionPane.showMessageDialog(null, "ID Inválido");
            //System.out.println(e);
        } catch (Exception e2) {
            JOptionPane.showMessageDialog(null, e2);
        }
    }

    private void alterarVeiculos() {

        /*String cepMasc = txtAssocCep.getText();
        String cep = cepMasc.replaceAll("\\D", "");

        String cnpjMasc = txtAssocCnpjCpf.getText();
        String cnpj = cnpjMasc.replaceAll("\\D", "");
        
        String valorParcUmMasc = txtParcUmAssoc.getText();
        String parcum = valorParcUmMasc.replaceAll("\\D", "");

        String valorParcMensal = txtParcMensalAssoc.getText();
        String mensalidade = valorParcMensal.replaceAll("\\D", "");*/
        String status = cboStatusVeic.getSelectedItem().toString();
        int statusnum = 0;

        if (status == "Inativo") {
            statusnum = 1;
        }
        String sql = "update tbveiculo set nomepropr=?, marca=?, modelo=?, ano=?, placa=?, renavan=?, idassoc=?, numcarretaveic=?, numdollyveic=?, idempr=?, veiculoexcluido=? where idveic = ?";

        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, txtNomeAssoc.getText());
            pst.setString(2, txtMarcaVeic.getText());
            pst.setString(3, txtModeloVeic.getText());
            pst.setString(4, txtAnoVeic.getText());
            pst.setString(5, txtPlacaVeic.getText());
            pst.setString(6, txtRenavanVeic.getText());
            pst.setString(7, txtIdAssoc.getText());
            pst.setString(8, cboNumCarretaVeic.getSelectedItem().toString());
            pst.setString(9, cboNumDollyVeic.getSelectedItem().toString());
            pst.setString(10, txtIdEmpresa.getText());
            pst.setString(11, String.valueOf(statusnum));

            /*int numeroConjTelaVeic = Integer.parseInt(txtNumConjVeic.getText());
            int numeroConjTelaAssoc = Integer.parseInt(cboNumConjAssoc.getSelectedItem().toString());

            if (numeroConjTelaVeic < numeroConjTelaAssoc) {
                pst.setString(11, String.valueOf(statusnum));
            } else {
                pst.setString(11, "0");
            }*/
            System.out.println("Status - 0 ativo 1 ativo : " + statusnum);

            pst.setString(12, txtIdVeic.getText());

            if ((txtNomeAssoc.getText().isEmpty()) || (txtMarcaVeic.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatorios.");

            } else {
                int adicionado = pst.executeUpdate();

                //System.out.println(adicionado);
                if (adicionado > 0) {

                    JOptionPane.showMessageDialog(null, "Dado(s) do veiculo alterado(s) com sucesso");

                    limpar();

                    btnAdicionar.setEnabled(false);
                    btnAlterar.setEnabled(true);
                    btnRemover.setEnabled(true);
                    btnPesquisar.setEnabled(true);
                    btnImprimir.setEnabled(true);

                }

            }

            //Validação dos campos obrigatorios
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void removerVeiculos() {
        int confirma = JOptionPane.showConfirmDialog(null, "Tem Certeza que deseja excluir este veiculo?", "Atenção", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {

            String sql = "update tbveiculo set veiculoexcluido = 1 where idveic = ?";

            try {
                pst = conexao.prepareStatement(sql);
                pst.setString(1, txtIdVeic.getText());
                int apagado = pst.executeUpdate();

                if (apagado > 0) {
                    JOptionPane.showMessageDialog(null, "Veiculo removido com sucesso");

                    limpar();

                    btnAdicionarVeic.setEnabled(true);
                    btnAlterarVeic.setEnabled(false);
                    btnRemoverVeic.setEnabled(false);
                    btnImprimirVeic.setEnabled(false);

                    setarNumeroDeConj();
                    pesquisar_veiculo();

                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    public void limparVeiculo() {
        //txtIdVeic.setText(null);
        //txtNomeAssocVeic.setText(null);
        //txtMarcaVeic.setText(null);
        txtModeloVeic.setText(null);
        //txtAnoVeic.setText(null);
        txtPlacaVeic.setText(null);
        txtRenavanVeic.setText(null);
        //txtIdAssoc.setText(null);        
        cboNumCarretaVeic.setSelectedIndex(0);
        cboNumDollyVeic.setSelectedIndex(0);

        btnAdicionarVeic.setEnabled(true);
        btnAlterarVeic.setEnabled(false);
        btnRemoverVeic.setEnabled(false);

    }

    public void limparVeiculoTodos() {
        txtIdVeic.setText(null);
        txtMarcaVeic.setText(null);
        txtModeloVeic.setText(null);
        txtAnoVeic.setText(null);
        txtPlacaVeic.setText(null);
        txtRenavanVeic.setText(null);

        btnAdicionarVeic.setEnabled(false);
        btnAlterarVeic.setEnabled(false);
        btnRemoverVeic.setEnabled(false);
        btnImprimirVeic.setEnabled(false);

    }

    public void setarNomeAssoc() {

        TelaAssociado associado = new TelaAssociado();

        String nomeassoc = associado.txtNomeAssoc.getText();

        String sql = "select * from tb where nomefanteempr = " + "'" + nomeassoc + "'";

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

    private void imprimirdoc_adesao_do_veic() {

        int confirma = JOptionPane.showConfirmDialog(null, "Imprimir documento de adesão?", "Atenção", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {
            //Imprimir relatorio com o framework JasperReport
            try {
                // usando a classe HashMap para criar um filtro
                HashMap filtro = new HashMap();
                filtro.put("idassoc", Integer.parseInt(txtIdAssoc.getText()));
                JasperPrint print = JasperFillManager.fillReport(getClass().getResourceAsStream("/impressao/termodeadesaoconjuntos.jasper"), filtro, conexao);
                //a linha abaixo apresenta o relatorio através da classe JasperViewer
                JasperViewer.viewReport(print, false);

            } catch (JRException e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    private void renavanJaCadast() {

        String renavan = txtRenavanVeic.getText();

        String sql = "select * from tbveiculo where renavan = " + "'" + renavan + "'";

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {

                JOptionPane.showMessageDialog(null, "Veiculo já cadastrado.");
                txtRenavanVeic.setText(null);

            } else {

            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void ativaCamposTxtAddVeic() {
        btnAdicionarVeic.setEnabled(true);
        txtMarcaVeic.setEnabled(true);
        txtModeloVeic.setEnabled(true);
        txtPlacaVeic.setEnabled(true);
        txtAnoVeic.setEnabled(true);
        txtRenavanVeic.setEnabled(true);
        cboNumCarretaVeic.setEnabled(true);
        cboNumDollyVeic.setEnabled(true);
    }

    private void inativarCamposTxtAddVeic() {
        btnAdicionarVeic.setEnabled(false);
        txtMarcaVeic.setEnabled(false);
        txtModeloVeic.setEnabled(false);
        txtPlacaVeic.setEnabled(false);
        txtAnoVeic.setEnabled(false);
        txtRenavanVeic.setEnabled(false);
        cboNumCarretaVeic.setEnabled(false);
        cboNumDollyVeic.setEnabled(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        txtNomeAssoc = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtAssocFantasia = new javax.swing.JTextField();
        txtAssocLograd = new javax.swing.JTextField();
        txtAssocBairro = new javax.swing.JTextField();
        btnAdicionar = new javax.swing.JButton();
        btnAlterar = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        txtAssocPesquisar = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        btnRemover = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        txtIdAssoc = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtAssocCidade = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtAssocFone1 = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtAssocFone2 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txtAssocFone3 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        txtAssocEmail = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        cboUf = new javax.swing.JComboBox<>();
        btnImprimir = new javax.swing.JButton();
        txtAssocCep = new javax.swing.JFormattedTextField();
        lblCnpjCpf = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        txtAssocCnpjCpf = new javax.swing.JFormattedTextField();
        cboCnpjCpf = new javax.swing.JComboBox<>();
        jLabel15 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        txtAssocCompl = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        txtAssocRepresentante = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblAssociados = new javax.swing.JTable();
        btnPesquisar = new javax.swing.JButton();
        btnLimpar = new javax.swing.JButton();
        txtDataCadAssoc = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        txtIdEmpresa = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        txtAdesaoAssoc = new javax.swing.JFormattedTextField();
        txtValorMensalAssoc = new javax.swing.JFormattedTextField();
        btnAddConjuntos = new javax.swing.JButton();
        jLabel22 = new javax.swing.JLabel();
        cboNumConjAssoc = new javax.swing.JComboBox<>();
        cboStatusAssoc = new javax.swing.JComboBox<>();
        lblStatusAssoc = new javax.swing.JLabel();
        txtVenc = new javax.swing.JFormattedTextField();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        cboFormaDePag = new javax.swing.JComboBox<>();
        jLabel25 = new javax.swing.JLabel();
        cboBanco = new javax.swing.JComboBox<>();
        jLabel26 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        txtDataCadVeic = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        txtMarcaVeic = new javax.swing.JTextField();
        btnAdicionarVeic = new javax.swing.JButton();
        jLabel29 = new javax.swing.JLabel();
        btnAlterarVeic = new javax.swing.JButton();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        txtRenavanVeic = new javax.swing.JFormattedTextField();
        txtAnoVeic = new javax.swing.JFormattedTextField();
        cboNumDollyVeic = new javax.swing.JComboBox<>();
        btnRemoverVeic = new javax.swing.JButton();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        txtIdVeic = new javax.swing.JTextField();
        txtModeloVeic = new javax.swing.JTextField();
        jLabel36 = new javax.swing.JLabel();
        txtNumConjVeic = new javax.swing.JTextField();
        txtConjCadastradosVeic = new javax.swing.JTextField();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        cboStatusVeic = new javax.swing.JComboBox<>();
        jLabel39 = new javax.swing.JLabel();
        lblStatusVeic = new javax.swing.JLabel();
        cboNumCarretaVeic = new javax.swing.JComboBox<>();
        btnImprimirVeic = new javax.swing.JButton();
        jLabel41 = new javax.swing.JLabel();
        txtPlacaVeic = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblVeiculos = new javax.swing.JTable();
        btnLimparVeic = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setTitle("Associado");
        setPreferredSize(new java.awt.Dimension(900, 719));

        jLabel1.setText("*Nome");

        txtNomeAssoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNomeAssocActionPerformed(evt);
            }
        });

        jLabel2.setText("Fantasia");

        jLabel3.setText("Endereço");

        jLabel4.setText("Bairro");

        txtAssocLograd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAssocLogradActionPerformed(evt);
            }
        });

        txtAssocBairro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAssocBairroActionPerformed(evt);
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

        jLabel7.setText("Id");

        txtIdAssoc.setEnabled(false);
        txtIdAssoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdAssocActionPerformed(evt);
            }
        });

        jLabel8.setText("Cidade");

        jLabel10.setText("CEP");

        jLabel11.setText("Fone1");

        jLabel12.setText("Fone2");

        jLabel13.setText("Fone3");

        jLabel14.setText("Email");

        txtAssocEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAssocEmailActionPerformed(evt);
            }
        });

        jLabel9.setText("UF");

        cboUf.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO" }));

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

        txtAssocCep.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtAssocCepFocusLost(evt);
            }
        });

        lblCnpjCpf.setText("CNPJ");

        jLabel16.setText("Tipo");

        txtAssocCnpjCpf.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtAssocCnpjCpfFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtAssocCnpjCpfFocusLost(evt);
            }
        });
        txtAssocCnpjCpf.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtAssocCnpjCpfKeyPressed(evt);
            }
        });

        cboCnpjCpf.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "CNPJ", "CPF" }));
        cboCnpjCpf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboCnpjCpfActionPerformed(evt);
            }
        });

        jLabel15.setText("Pesquisar Associado");

        jLabel17.setText("Complemento");

        txtAssocCompl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAssocComplActionPerformed(evt);
            }
        });

        jLabel18.setText("Representante");

        txtAssocRepresentante.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAssocRepresentanteActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Associados Cadastrados"));

        tblAssociados = new javax.swing.JTable(){
            public boolean isCellEditable(int rowIndex, int colIndex){
                return false;
            }
        };
        tblAssociados.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Associado", "Fantasia", "Representante", "CNPJ / CPF", "Email"
            }
        ));
        tblAssociados.getTableHeader().setReorderingAllowed(false);
        tblAssociados.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblAssociadosMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblAssociados);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(24, Short.MAX_VALUE)
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

        txtDataCadAssoc.setEnabled(false);
        txtDataCadAssoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDataCadAssocActionPerformed(evt);
            }
        });

        jLabel19.setText("Data:");

        jLabel20.setText("Adesão");

        jLabel21.setText("Parc. Mensais");

        txtAdesaoAssoc.setText("2000");
        txtAdesaoAssoc.setToolTipText("");
        txtAdesaoAssoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAdesaoAssocActionPerformed(evt);
            }
        });

        txtValorMensalAssoc.setText("300");
        txtValorMensalAssoc.setToolTipText("");
        txtValorMensalAssoc.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                txtValorMensalAssocInputMethodTextChanged(evt);
            }
        });
        txtValorMensalAssoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtValorMensalAssocActionPerformed(evt);
            }
        });

        btnAddConjuntos.setText("Adicionar Conjuntos");
        btnAddConjuntos.setEnabled(false);
        btnAddConjuntos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddConjuntosActionPerformed(evt);
            }
        });

        jLabel22.setText("Nº Conj");

        cboNumConjAssoc.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" }));

        cboStatusAssoc.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Ativo", "Inativo" }));
        cboStatusAssoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboStatusAssocActionPerformed(evt);
            }
        });

        lblStatusAssoc.setText("Status");

        jLabel23.setText("Venc.");

        jLabel24.setText("Forma Pag");

        cboFormaDePag.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Boleto" }));

        jLabel25.setText("Banco");

        jLabel26.setText("Status");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Adicionar Veículo"));

        txtDataCadVeic.setEnabled(false);
        txtDataCadVeic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDataCadVeicActionPerformed(evt);
            }
        });

        jLabel27.setText("Marca");

        jLabel28.setText("Data:");

        txtMarcaVeic.setEnabled(false);

        btnAdicionarVeic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/sistempresa/icones/create.png"))); // NOI18N
        btnAdicionarVeic.setToolTipText("Adicionar Usuário");
        btnAdicionarVeic.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAdicionarVeic.setEnabled(false);
        btnAdicionarVeic.setPreferredSize(new java.awt.Dimension(80, 80));
        btnAdicionarVeic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdicionarVeicActionPerformed(evt);
            }
        });

        jLabel29.setText("RENAVAN");

        btnAlterarVeic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/sistempresa/icones/update.png"))); // NOI18N
        btnAlterarVeic.setToolTipText("Alterar");
        btnAlterarVeic.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAlterarVeic.setEnabled(false);
        btnAlterarVeic.setPreferredSize(new java.awt.Dimension(80, 80));
        btnAlterarVeic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAlterarVeicActionPerformed(evt);
            }
        });

        jLabel30.setText("Ano");

        jLabel31.setText("* Campos obrigatórios");

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

        cboNumDollyVeic.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", " ", " " }));
        cboNumDollyVeic.setEnabled(false);

        btnRemoverVeic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/sistempresa/icones/delete.png"))); // NOI18N
        btnRemoverVeic.setToolTipText("Apagar");
        btnRemoverVeic.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnRemoverVeic.setEnabled(false);
        btnRemoverVeic.setPreferredSize(new java.awt.Dimension(80, 80));
        btnRemoverVeic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoverVeicActionPerformed(evt);
            }
        });

        jLabel33.setText("Modelo");

        jLabel34.setText("Id");

        txtIdVeic.setEnabled(false);
        txtIdVeic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdVeicActionPerformed(evt);
            }
        });

        txtModeloVeic.setEnabled(false);

        jLabel36.setText("Nº Conj");

        txtNumConjVeic.setEnabled(false);

        jLabel37.setText("Status");

        jLabel38.setText("Doly");

        cboStatusVeic.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Ativo", "Inativo", " " }));
        cboStatusVeic.setEnabled(false);

        jLabel39.setText("Carreta");

        lblStatusVeic.setText("status2");

        cboNumCarretaVeic.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", " " }));
        cboNumCarretaVeic.setEnabled(false);

        btnImprimirVeic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/sistempresa/icones/imprimir.png"))); // NOI18N
        btnImprimirVeic.setToolTipText("Imprimir Doc de Adesão");
        btnImprimirVeic.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnImprimirVeic.setEnabled(false);
        btnImprimirVeic.setPreferredSize(new java.awt.Dimension(80, 80));
        btnImprimirVeic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImprimirVeicActionPerformed(evt);
            }
        });

        jLabel41.setText("Placa");

        txtPlacaVeic.setEnabled(false);
        txtPlacaVeic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPlacaVeicActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Conjuntos"));

        tblVeiculos = new javax.swing.JTable(){
            public boolean isCellEditable(int rowIndex, int colIndex){
                return false;
            }
        };
        tblVeiculos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Marca", "Modelo", "Ano", "Placa", "Renavan"
            }
        ));
        tblVeiculos.getTableHeader().setReorderingAllowed(false);
        tblVeiculos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblVeiculosMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblVeiculos);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 519, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(227, 227, 227))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        btnLimparVeic.setText("Limpar Campos");
        btnLimparVeic.setEnabled(false);
        btnLimparVeic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimparVeicActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(jLabel31))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 550, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel27)
                                    .addComponent(jLabel41)
                                    .addComponent(jLabel39))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(txtMarcaVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel33)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtModeloVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(txtIdVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel28)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtDataCadVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel36)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtNumConjVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel37)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cboStatusVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(txtPlacaVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel30)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtAnoVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel29)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtRenavanVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(cboNumCarretaVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(btnAdicionarVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(btnAlterarVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(btnRemoverVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(btnImprimirVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(jLabel38)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cboNumDollyVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(lblStatusVeic))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(txtConjCadastradosVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnLimparVeic)
                .addGap(56, 56, 56))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jLabel31)
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel34)
                    .addComponent(txtIdVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel28)
                    .addComponent(txtDataCadVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel36)
                    .addComponent(txtNumConjVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel37)
                    .addComponent(cboStatusVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMarcaVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel27)
                    .addComponent(jLabel33)
                    .addComponent(txtModeloVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel41)
                    .addComponent(txtPlacaVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel30)
                    .addComponent(txtAnoVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel29)
                    .addComponent(txtRenavanVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel39)
                    .addComponent(cboNumCarretaVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel38)
                    .addComponent(cboNumDollyVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnAdicionarVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnImprimirVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAlterarVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemoverVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnLimparVeic)
                .addGap(36, 36, 36)
                .addComponent(txtConjCadastradosVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblStatusVeic)
                .addContainerGap(78, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(186, 186, 186)
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtAssocPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel6))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtIdEmpresa, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblStatusAssoc))
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(49, 49, 49)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel17, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtAssocFone1, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(31, 31, 31)
                                        .addComponent(jLabel12)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtAssocFone2, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel13)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtAssocFone3, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtAssocCep, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel8)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtAssocCidade, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel9)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(cboUf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(txtAssocEmail, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtAssocBairro, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtAssocCompl, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtAssocLograd, javax.swing.GroupLayout.PREFERRED_SIZE, 650, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(btnPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(btnRemover, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(btnImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(10, 10, 10)
                                                .addComponent(btnLimpar))
                                            .addComponent(btnAddConjuntos)))))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(lblCnpjCpf, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel18, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel20, javax.swing.GroupLayout.Alignment.TRAILING))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jLabel24)
                                        .addGap(11, 11, 11)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(2, 2, 2)
                                        .addComponent(txtAssocCnpjCpf, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel16)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(cboCnpjCpf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel19)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtDataCadAssoc, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel26)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cboStatusAssoc, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(txtNomeAssoc)
                                    .addComponent(txtAssocFantasia)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtAdesaoAssoc, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel21)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtValorMensalAssoc, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel22)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(cboNumConjAssoc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel23)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtVenc, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(txtAssocRepresentante, javax.swing.GroupLayout.PREFERRED_SIZE, 643, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(cboFormaDePag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel25)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cboBanco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtIdAssoc, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel6)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtAssocPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel15)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cboCnpjCpf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtAssocCnpjCpf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel16)
                            .addComponent(lblCnpjCpf)
                            .addComponent(jLabel7)
                            .addComponent(txtIdAssoc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel19)
                            .addComponent(txtDataCadAssoc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cboStatusAssoc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel26))
                        .addGap(9, 9, 9)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(txtNomeAssoc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtAssocFantasia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel18)
                            .addComponent(txtAssocRepresentante, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel20)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel21)
                                .addComponent(txtAdesaoAssoc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtValorMensalAssoc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel22)
                                .addComponent(cboNumConjAssoc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtVenc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel23)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel24)
                            .addComponent(cboFormaDePag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel25)
                            .addComponent(cboBanco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(txtAssocCep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8)
                            .addComponent(txtAssocCidade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9)
                            .addComponent(cboUf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtAssocLograd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtAssocCompl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtAssocBairro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtAssocFone1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12)
                            .addComponent(txtAssocFone2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13)
                            .addComponent(txtAssocFone3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtAssocEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel14))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnRemover, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(17, 17, 17)
                                .addComponent(btnAddConjuntos)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnLimpar))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtIdEmpresa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblStatusAssoc))
                .addContainerGap(65, Short.MAX_VALUE))
        );

        btnImprimir.getAccessibleContext().setAccessibleDescription("Imprimir Documento de Adesão");
        btnPesquisar.getAccessibleContext().setAccessibleDescription("Pesquisar Por ID");

        setBounds(0, 0, 1461, 761);
    }// </editor-fold>//GEN-END:initComponents

    private void txtNomeAssocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNomeAssocActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNomeAssocActionPerformed

    private void btnAdicionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarActionPerformed
        adicionar();
    }//GEN-LAST:event_btnAdicionarActionPerformed

    private void btnAlterarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlterarActionPerformed
        alterarAssociado();
    }//GEN-LAST:event_btnAlterarActionPerformed

    private void txtAssocPesquisarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAssocPesquisarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAssocPesquisarActionPerformed

    private void btnRemoverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoverActionPerformed
        remover();
    }//GEN-LAST:event_btnRemoverActionPerformed

    //metodo do tipo em tempo de execução conforme for informado a informação ele é acionado
    private void txtAssocPesquisarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtAssocPesquisarKeyReleased
        //Chamar o metodo pesquisa clientes 
        pesquisar_cliente();
    }//GEN-LAST:event_txtAssocPesquisarKeyReleased

    //Evento que sera usado para setar os campos da tabela clicando com o botão do mouse
    private void tblAssociadosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblAssociadosMouseClicked
        // Chamando o metodo para setar os campos
        setar_campos();
    }//GEN-LAST:event_tblAssociadosMouseClicked

    private void txtIdAssocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdAssocActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIdAssocActionPerformed

    private void txtAssocEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAssocEmailActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAssocEmailActionPerformed

    private void txtAssocBairroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAssocBairroActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAssocBairroActionPerformed

    private void txtAssocLogradActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAssocLogradActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAssocLogradActionPerformed

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        imprimirdoc_adesao();
    }//GEN-LAST:event_btnImprimirActionPerformed

    private void txtAssocCnpjCpfKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtAssocCnpjCpfKeyPressed

    }//GEN-LAST:event_txtAssocCnpjCpfKeyPressed

    private void txtAssocCnpjCpfFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtAssocCnpjCpfFocusLost

        if (cboCnpjCpf.getSelectedItem().equals("CNPJ")) {
            if (!txtAssocCnpjCpf.getText().equals("  .   .   /    -  ")) {
                consultarCNPJCPF();

            }
        } else if (cboCnpjCpf.getSelectedItem().equals("CPF")) {

            if (!txtAssocCnpjCpf.getText().equals("   .   .   -  ")) {
                consultarCNPJCPF();

            }
        }


    }//GEN-LAST:event_txtAssocCnpjCpfFocusLost

    private void txtAssocCepFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtAssocCepFocusLost
        if (txtAssocCep.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Informe o CEP");
        } else {
            buscarCep();

        }
    }//GEN-LAST:event_txtAssocCepFocusLost

    private void cboCnpjCpfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboCnpjCpfActionPerformed

        int tipoDoc = cboCnpjCpf.getSelectedIndex();

        switch (tipoDoc) {
            case 0:
                txtAssocCnpjCpf.setFormatterFactory(new DefaultFormatterFactory(CNPJMask));
                lblCnpjCpf.setText("CNPJ");
                break;
            case 1:
                txtAssocCnpjCpf.setFormatterFactory(new DefaultFormatterFactory(CPFMask));
                lblCnpjCpf.setText("CPF");
                break;
        }


    }//GEN-LAST:event_cboCnpjCpfActionPerformed

    private void txtAssocComplActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAssocComplActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAssocComplActionPerformed

    private void txtAssocRepresentanteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAssocRepresentanteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAssocRepresentanteActionPerformed

    private void btnPesquisarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPesquisarActionPerformed
        //consultar();
        pesquisarPorCnpj();
    }//GEN-LAST:event_btnPesquisarActionPerformed

    private void txtAssocCnpjCpfFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtAssocCnpjCpfFocusGained


    }//GEN-LAST:event_txtAssocCnpjCpfFocusGained

    private void btnLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparActionPerformed
        limpar();
        btnImprimir.setEnabled(false);
    }//GEN-LAST:event_btnLimparActionPerformed

    private void txtDataCadAssocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDataCadAssocActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDataCadAssocActionPerformed

    private void txtValorMensalAssocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtValorMensalAssocActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtValorMensalAssocActionPerformed

    private void txtAdesaoAssocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAdesaoAssocActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAdesaoAssocActionPerformed

    private void txtAssocPesquisarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtAssocPesquisarMouseClicked
        pesquisar_cliente();
    }//GEN-LAST:event_txtAssocPesquisarMouseClicked

    private void cboStatusAssocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboStatusAssocActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cboStatusAssocActionPerformed

    private void txtDataCadVeicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDataCadVeicActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDataCadVeicActionPerformed

    private void btnAdicionarVeicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarVeicActionPerformed
        adicionarVeiculo();
    }//GEN-LAST:event_btnAdicionarVeicActionPerformed

    private void btnAlterarVeicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlterarVeicActionPerformed
        alterarVeiculos();
    }//GEN-LAST:event_btnAlterarVeicActionPerformed

    private void txtRenavanVeicFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtRenavanVeicFocusLost
        renavanJaCadast();
    }//GEN-LAST:event_txtRenavanVeicFocusLost

    private void txtAnoVeicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAnoVeicActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAnoVeicActionPerformed

    private void btnRemoverVeicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoverVeicActionPerformed
        removerVeiculos();
    }//GEN-LAST:event_btnRemoverVeicActionPerformed

    private void txtIdVeicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdVeicActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIdVeicActionPerformed

    private void btnImprimirVeicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirVeicActionPerformed
        imprimirdoc_adesao_do_veic();
    }//GEN-LAST:event_btnImprimirVeicActionPerformed

    private void txtPlacaVeicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPlacaVeicActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPlacaVeicActionPerformed

    private void tblVeiculosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblVeiculosMouseClicked
        // Chamando o metodo para setar os campos
        setar_camposVeiculos();
    }//GEN-LAST:event_tblVeiculosMouseClicked

    private void btnLimparVeicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparVeicActionPerformed
        // TODO add your handling code here:
        limparVeiculoTodos();
        inativarCamposTxtAddVeic();
    }//GEN-LAST:event_btnLimparVeicActionPerformed

    private void btnAddConjuntosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddConjuntosActionPerformed
        TelaVeiculo veiculo = new TelaVeiculo();
        getParent().add(veiculo);
        veiculo.setVisible(true);
        dispose();
    }//GEN-LAST:event_btnAddConjuntosActionPerformed

    private void txtValorMensalAssocInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_txtValorMensalAssocInputMethodTextChanged

    }//GEN-LAST:event_txtValorMensalAssocInputMethodTextChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddConjuntos;
    private javax.swing.JButton btnAdicionar;
    private javax.swing.JButton btnAdicionarVeic;
    private javax.swing.JButton btnAlterar;
    private javax.swing.JButton btnAlterarVeic;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnImprimirVeic;
    private javax.swing.JButton btnLimpar;
    private javax.swing.JButton btnLimparVeic;
    private javax.swing.JButton btnPesquisar;
    private javax.swing.JButton btnRemover;
    private javax.swing.JButton btnRemoverVeic;
    private javax.swing.JComboBox<String> cboBanco;
    private javax.swing.JComboBox<String> cboCnpjCpf;
    private javax.swing.JComboBox<String> cboFormaDePag;
    private javax.swing.JComboBox<String> cboNumCarretaVeic;
    public static javax.swing.JComboBox<String> cboNumConjAssoc;
    private javax.swing.JComboBox<String> cboNumDollyVeic;
    private javax.swing.JComboBox<String> cboStatusAssoc;
    private javax.swing.JComboBox<String> cboStatusVeic;
    private javax.swing.JComboBox<String> cboUf;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
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
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel41;
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
    private javax.swing.JLabel lblCnpjCpf;
    private javax.swing.JLabel lblStatusAssoc;
    private javax.swing.JLabel lblStatusVeic;
    private javax.swing.JTable tblAssociados;
    private javax.swing.JTable tblVeiculos;
    private javax.swing.JFormattedTextField txtAdesaoAssoc;
    private javax.swing.JFormattedTextField txtAnoVeic;
    private javax.swing.JTextField txtAssocBairro;
    private javax.swing.JFormattedTextField txtAssocCep;
    private javax.swing.JTextField txtAssocCidade;
    private javax.swing.JFormattedTextField txtAssocCnpjCpf;
    private javax.swing.JTextField txtAssocCompl;
    private javax.swing.JTextField txtAssocEmail;
    private javax.swing.JTextField txtAssocFantasia;
    private javax.swing.JTextField txtAssocFone1;
    private javax.swing.JTextField txtAssocFone2;
    private javax.swing.JTextField txtAssocFone3;
    private javax.swing.JTextField txtAssocLograd;
    private javax.swing.JTextField txtAssocPesquisar;
    private javax.swing.JTextField txtAssocRepresentante;
    private javax.swing.JTextField txtConjCadastradosVeic;
    private javax.swing.JTextField txtDataCadAssoc;
    private javax.swing.JTextField txtDataCadVeic;
    public static javax.swing.JTextField txtIdAssoc;
    private javax.swing.JTextField txtIdEmpresa;
    private javax.swing.JTextField txtIdVeic;
    private javax.swing.JTextField txtMarcaVeic;
    private javax.swing.JTextField txtModeloVeic;
    public static javax.swing.JTextField txtNomeAssoc;
    private javax.swing.JTextField txtNumConjVeic;
    private javax.swing.JTextField txtPlacaVeic;
    private javax.swing.JFormattedTextField txtRenavanVeic;
    private javax.swing.JFormattedTextField txtValorMensalAssoc;
    private javax.swing.JFormattedTextField txtVenc;
    // End of variables declaration//GEN-END:variables
}
