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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;

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

public class TelaFornecedor extends javax.swing.JInternalFrame {

    //Usando a variavel de conexao do DAL
    Connection conexao = null;
    //Criando variaveis especiais para conexao com o banco 
    //Prepared Stetament e ResultSet são Frameworks do pacote java.sql
    // e servem para preparar e executar as instruções SQL
    PreparedStatement pst = null;
    ResultSet rs = null;

    private MaskFormatter CNPJMask, CPFMask, CEPMask, ValorParcUm, ValorMensal;
    private MaskFormatter Fone1Mask, Fone2Mask, Fone3Mask;

    /**
     * Creates new form TelaCliente
     */
    public TelaFornecedor() {
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

        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        txtFornCep.setFormatterFactory(new DefaultFormatterFactory(CEPMask));
        txtFornCnpjCpf.setFormatterFactory(new DefaultFormatterFactory(CNPJMask));
        //txtParcUmAssoc.setFormatterFactory(new DefaultFormatterFactory(ValorParcUm));
        //txtParcMensalAssoc.setFormatterFactory(new DefaultFormatterFactory(ValorMensal));

        setarIdEmpresa();

        txtIdEmpresa.setVisible(false);

    }

    private void adicionar() {

        String sql = "insert into tbfornecedor(NOMEFORNECEDOR, NOMEFANT, REPRESENTANTE, UF, CIDADE, BAIRRO, LOGRADOURO, COMPLEMENTO, CEP, FONE1, FONE2, FONE3, EMAIL, CNPJCPF, TIPODOCUMENTO, idempr) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            pst = conexao.prepareStatement(sql);

            pst.setString(1, txtNomeForn.getText());
            pst.setString(2, txtFornFantasia.getText());
            pst.setString(3, txtFornRepr.getText());
            pst.setString(4, cboUf.getSelectedItem().toString());
            pst.setString(5, txtFornCidade.getText());
            pst.setString(6, txtFornBairro.getText());
            pst.setString(7, txtFornLograd.getText());
            pst.setString(8, txtFornCompl.getText());
            pst.setString(9, txtFornCep.getText());
            pst.setString(10, txtFornFone1.getText());
            pst.setString(11, txtFornFone2.getText());
            pst.setString(12, txtFornFone3.getText());
            pst.setString(13, txtFornEmail.getText());
            pst.setString(14, txtFornCnpjCpf.getText());
            pst.setString(15, cboCnpjCpf.getSelectedItem().toString());
            pst.setString(16, txtIdEmpresa.getText());            

            //Validação dos campos obrigatorios
            if ((txtNomeForn.getText().isEmpty()) || (txtFornCnpjCpf.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatorios.");

            } else {
                int adicionado = pst.executeUpdate();

                //System.out.println(adicionado);
                if (adicionado > 0) {

                    JOptionPane.showMessageDialog(null, "Forncedor adicionado com sucesso");

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

        String sql = "select idforn as ID, NOMEFORNECEDOR as Fornecedor, NOMEFANT as Fantasia, REPRESENTANTE as Representante, CNPJCPF as CNPJ, EMAIL AS Email from tbfornecedor where NOMEFORNECEDOR like ?";

        try {
            pst = conexao.prepareStatement(sql);
            //Passando o conteudo da caixa de pesquisa para o ?
            //atenção ao "%" - continuação da pesquisa sql
            pst.setString(1, txtFornPesquisar.getText() + "%");
            rs = pst.executeQuery();
            // a linha abaixo usa a biblioteca rs2xml.jar para preencher a tabela 
            tblFornecedor.setModel(DbUtils.resultSetToTableModel(rs));

            tblFornecedor.getColumnModel().getColumn(0).setMaxWidth(50);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    //Metodo para setar os campos do formulario com o conteudo da tabela    
    public void setar_campos() {

        int setar = tblFornecedor.getSelectedRow();

        txtFornId.setText(tblFornecedor.getModel().getValueAt(setar, 0).toString());

        String id_forn = txtFornId.getText();

        String sql = "select idforn, date_format(DATACADASTFORN,'%d/%m/%Y'), NOMEFORNECEDOR, NOMEFANT, REPRESENTANTE, UF, CIDADE, BAIRRO, LOGRADOURO, COMPLEMENTO, CEP, FONE1, FONE2, FONE3, EMAIL, TIPODOCUMENTO, CNPJCPF from tbfornecedor where idforn =" + id_forn;

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                txtFornId.setText(rs.getString(1));
                txtDataCadForn.setText(rs.getString(2));
                txtNomeForn.setText(rs.getString(3));
                txtFornFantasia.setText(rs.getString(4));
                txtFornRepr.setText(rs.getString(5));
                cboUf.setSelectedItem(rs.getString(6));
                txtFornCidade.setText(rs.getString(7));
                txtFornBairro.setText(rs.getString(8));
                txtFornLograd.setText(rs.getString(9));
                txtFornCompl.setText(rs.getString(10));
                txtFornCep.setText(rs.getString(11));
                txtFornFone1.setText(rs.getString(12));
                txtFornFone2.setText(rs.getString(13));
                txtFornFone3.setText(rs.getString(14));
                txtFornEmail.setText(rs.getString(15));
                cboCnpjCpf.setSelectedItem(rs.getString(16));
                txtFornCnpjCpf.setText(rs.getString(17));                

                btnAdicionar.setEnabled(true);
                btnAlterar.setEnabled(true);
                btnRemover.setEnabled(true);
                btnPesquisar.setEnabled(true);
                btnImprimir.setEnabled(true);
                btnAddConjuntos.setEnabled(true);
                btnLimpar.setEnabled(true);

            } else {
                JOptionPane.showMessageDialog(null, "Fornecedor não cadastrado.");
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

        String id_forn = JOptionPane.showInputDialog("Informe o ID do Fornecedor?");

        String sql = "\"select idforn, date_format(DATACADASTFORN,'%d/%m/%Y'), NOMEFORNECEDOR, NOMEFANT, REPRESENTANTE, UF, CIDADE, BAIRRO, LOGRADOURO, COMPLEMENTO, CEP, FONE1, FONE2, FONE3, EMAIL, TIPODOCUMENTO, CNPJCPF, from tbfornecedor where idforn =" + id_forn;

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                txtFornId.setText(rs.getString(1));
                txtDataCadForn.setText(rs.getString(2));
                txtNomeForn.setText(rs.getString(3));
                txtFornFantasia.setText(rs.getString(4));
                txtFornRepr.setText(rs.getString(5));
                cboUf.setSelectedItem(rs.getString(6));
                txtFornCidade.setText(rs.getString(7));
                txtFornBairro.setText(rs.getString(8));
                txtFornLograd.setText(rs.getString(9));
                txtFornCompl.setText(rs.getString(10));
                txtFornCep.setText(rs.getString(11));
                txtFornFone1.setText(rs.getString(12));
                txtFornFone2.setText(rs.getString(13));
                txtFornFone3.setText(rs.getString(14));
                txtFornEmail.setText(rs.getString(15));
                cboCnpjCpf.setSelectedItem(rs.getString(16));
                txtFornCnpjCpf.setText(rs.getString(17));
                
                btnAdicionar.setEnabled(true);
                btnAlterar.setEnabled(true);
                btnRemover.setEnabled(true);
                btnPesquisar.setEnabled(true);
                btnImprimir.setEnabled(true);
                btnAddConjuntos.setEnabled(false);
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

    private void consultarCNPJCPF() {

        String cnpjcpf = txtFornCnpjCpf.getText().toString();

        String sql = "select * from tbfornecedor where CNPJCPF = " + "'" + cnpjcpf + "'";

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {

                JOptionPane.showMessageDialog(null, "CNPJ / CPF ja cadastrado.");
                limpar();

            } else {
                buscarCNPJ();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void alterar() {

        /*String cepMasc = txtAssocCep.getText();
        String cep = cepMasc.replaceAll("\\D", "");

        String cnpjMasc = txtAssocCnpjCpf.getText();
        String cnpj = cnpjMasc.replaceAll("\\D", "");
        
        String valorParcUmMasc = txtParcUmAssoc.getText();
        String parcum = valorParcUmMasc.replaceAll("\\D", "");

        String valorParcMensal = txtParcMensalAssoc.getText();
        String mensalidade = valorParcMensal.replaceAll("\\D", "");*/
        String sql = "update tbfornecedor set NOMEFORNECEDOR=?, NOMEFANT=?, REPRESENTANTE=?, UF=?, CIDADE=?, BAIRRO=?, LOGRADOURO=?, COMPLEMENTO=?, CEP=?, FONE1=?, FONE2=?, FONE3=?, EMAIL=?, TIPODOCUMENTO=?, CNPJCPF=? where idforn = ?";

        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, txtNomeForn.getText());
            pst.setString(2, txtFornFantasia.getText());
            pst.setString(3, txtFornRepr.getText());
            pst.setString(4, cboUf.getSelectedItem().toString());
            pst.setString(5, txtFornCidade.getText());
            pst.setString(6, txtFornBairro.getText());
            pst.setString(7, txtFornLograd.getText());
            pst.setString(8, txtFornCompl.getText());
            pst.setString(9, txtFornCep.getText());
            pst.setString(10, txtFornFone1.getText());
            pst.setString(11, txtFornFone2.getText());
            pst.setString(12, txtFornFone3.getText());
            pst.setString(13, txtFornEmail.getText());
            pst.setString(14, cboCnpjCpf.getSelectedItem().toString());
            pst.setString(15, txtFornCnpjCpf.getText());            
            pst.setString(19, txtFornId.getText());

            //Validação dos campos obrigatorios
            if ((txtNomeForn.getText().isEmpty()) || (txtFornLograd.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatorios.");

            } else {
                int adicionado = pst.executeUpdate();

                //System.out.println(adicionado);
                if (adicionado > 0) {

                    JOptionPane.showMessageDialog(null, "Dados do associado alterado com sucesso");

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
        int confirma = JOptionPane.showConfirmDialog(null, "Tem Certeza que deseja excluir este associado", "Atenção", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {
            String sql = "delete from tbassociado where IDASSOC=?";

            try {
                pst = conexao.prepareStatement(sql);
                pst.setString(1, txtFornId.getText());
                int apagado = pst.executeUpdate();

                if (apagado > 0) {
                    JOptionPane.showMessageDialog(null, "Associado removido com sucesso");

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

    private void buscarCep() {

        String logradouro = "";
        String tipologradouro = "";
        String resultado = null;
        String cep = txtFornCep.getText();

        try {
            URL url = new URL("http://cep.republicavirtual.com.br/web_cep.php?cep=" + cep + "&formato=xml");
            SAXReader xml = new SAXReader();
            Document documento = xml.read(url);
            Element root = documento.getRootElement();

            for (Iterator<Element> it = root.elementIterator(); it.hasNext();) {
                Element element = it.next();

                if (element.getQualifiedName().equals("bairro")) {
                    txtFornBairro.setText(element.getText());
                }
                if (element.getQualifiedName().equals("cidade")) {
                    txtFornCidade.setText(element.getText());
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
            txtFornLograd.setText(tipologradouro + " " + logradouro);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void buscarCNPJ() throws MalformedURLException {

        String cnpjMasc = txtFornCnpjCpf.getText();
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

                txtNomeForn.setText((String) jSONObject.get("nome"));
                txtFornFantasia.setText((String) jSONObject.get("fantasia"));
                txtFornCep.setText((String) jSONObject.get("cep"));
                logradouro = (String) jSONObject.get("logradouro");
                numero = (String) jSONObject.get("numero");
                txtFornLograd.setText(logradouro + " Nº " + numero);
                txtFornCompl.setText((String) jSONObject.get("complemento"));
                txtFornBairro.setText((String) jSONObject.get("bairro"));
                txtFornCidade.setText((String) jSONObject.get("municipio"));
                cboUf.setSelectedItem((String) jSONObject.get("uf"));
                txtFornFone1.setText((String) jSONObject.get("telefone"));
                txtFornEmail.setText((String) jSONObject.get("email"));
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
        txtFornId.setText(null);
        txtNomeForn.setText(null);
        txtFornFantasia.setText(null);
        txtFornRepr.setText(null);
        cboUf.setSelectedIndex(0);
        txtFornCidade.setText(null);
        txtFornBairro.setText(null);
        txtFornLograd.setText(null);
        txtFornCompl.setText(null);
        txtFornCep.setText(null);
        txtFornFone1.setText(null);
        txtFornFone2.setText(null);
        txtFornFone3.setText(null);
        txtFornEmail.setText(null);
        txtFornCnpjCpf.setText(null);
        cboCnpjCpf.setSelectedIndex(0);        

        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(false);
        btnRemover.setEnabled(false);
        btnAddConjuntos.setEnabled(false);
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

    private void imprimirdoc_adesao() {

        int confirma = JOptionPane.showConfirmDialog(null, "Imprimir documento de adesão?", "Atenção", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {
            //Imprimir relatorio com o framework JasperReport
            try {
                // usando a classe HashMap para criar um filtro
                HashMap filtro = new HashMap();
                filtro.put("idassoc", Integer.parseInt(txtFornId.getText()));
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
                txtFornId.setText(rs.getString(1));

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

        jLabel1 = new javax.swing.JLabel();
        txtNomeForn = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtFornFantasia = new javax.swing.JTextField();
        txtFornLograd = new javax.swing.JTextField();
        txtFornBairro = new javax.swing.JTextField();
        btnAdicionar = new javax.swing.JButton();
        btnAlterar = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        txtFornPesquisar = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        btnRemover = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        txtFornId = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtFornCidade = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtFornFone1 = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtFornFone2 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txtFornFone3 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        txtFornEmail = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        cboUf = new javax.swing.JComboBox<>();
        btnImprimir = new javax.swing.JButton();
        txtFornCep = new javax.swing.JFormattedTextField();
        lblCnpjCpf = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        txtFornCnpjCpf = new javax.swing.JFormattedTextField();
        cboCnpjCpf = new javax.swing.JComboBox<>();
        jLabel15 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        txtFornCompl = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        txtFornRepr = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblFornecedor = new javax.swing.JTable();
        btnPesquisar = new javax.swing.JButton();
        btnLimpar = new javax.swing.JButton();
        txtDataCadForn = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        txtIdEmpresa = new javax.swing.JTextField();
        btnAddConjuntos = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setTitle("Associado");
        setPreferredSize(new java.awt.Dimension(900, 719));

        jLabel1.setText("*Nome");

        txtNomeForn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNomeFornActionPerformed(evt);
            }
        });

        jLabel2.setText("Fantasia");

        jLabel3.setText("Endereço");

        jLabel4.setText("Bairro");

        txtFornLograd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFornLogradActionPerformed(evt);
            }
        });

        txtFornBairro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFornBairroActionPerformed(evt);
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

        txtFornPesquisar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtFornPesquisarMouseClicked(evt);
            }
        });
        txtFornPesquisar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFornPesquisarActionPerformed(evt);
            }
        });
        txtFornPesquisar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtFornPesquisarKeyReleased(evt);
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

        txtFornId.setEnabled(false);
        txtFornId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFornIdActionPerformed(evt);
            }
        });

        jLabel8.setText("Cidade");

        jLabel10.setText("CEP");

        jLabel11.setText("Fone1");

        jLabel12.setText("Fone2");

        jLabel13.setText("Fone3");

        jLabel14.setText("Email");

        txtFornEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFornEmailActionPerformed(evt);
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

        txtFornCep.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtFornCepFocusLost(evt);
            }
        });

        lblCnpjCpf.setText("CNPJ");

        jLabel16.setText("Tipo");

        txtFornCnpjCpf.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtFornCnpjCpfFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtFornCnpjCpfFocusLost(evt);
            }
        });
        txtFornCnpjCpf.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtFornCnpjCpfKeyPressed(evt);
            }
        });

        cboCnpjCpf.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "CNPJ", "CPF" }));
        cboCnpjCpf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboCnpjCpfActionPerformed(evt);
            }
        });

        jLabel15.setText("Pesquisar Fornecedor");

        jLabel17.setText("Complemento");

        txtFornCompl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFornComplActionPerformed(evt);
            }
        });

        jLabel18.setText("Representante");

        txtFornRepr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFornReprActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Associados Cadastrados"));

        tblFornecedor = new javax.swing.JTable(){
            public boolean isCellEditable(int rowIndex, int colIndex){
                return false;
            }
        };
        tblFornecedor.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Fornecedor", "Fantasia", "Representante", "CNPJ / CPF", "Email"
            }
        ));
        tblFornecedor.getTableHeader().setReorderingAllowed(false);
        tblFornecedor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblFornecedorMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblFornecedor);

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

        txtDataCadForn.setEnabled(false);
        txtDataCadForn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDataCadFornActionPerformed(evt);
            }
        });

        jLabel19.setText("Data:");

        btnAddConjuntos.setText("Adicionar Conjuntos");
        btnAddConjuntos.setEnabled(false);
        btnAddConjuntos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddConjuntosActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtIdEmpresa, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(90, 90, 90)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(lblCnpjCpf, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel18, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel17, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtNomeForn)
                                            .addComponent(txtFornBairro)
                                            .addComponent(txtFornCompl)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(txtFornCep, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(jLabel8)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(txtFornCidade, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(jLabel9)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(cboUf, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                            .addComponent(txtFornRepr)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(2, 2, 2)
                                                .addComponent(txtFornCnpjCpf, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jLabel16)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(cboCnpjCpf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jLabel19)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(txtDataCadForn, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(txtFornFantasia)
                                            .addComponent(txtFornLograd)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(txtFornFone1, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jLabel12)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(txtFornFone2, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jLabel13)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(txtFornFone3, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(txtFornEmail)))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(42, 42, 42)
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
                                            .addComponent(btnAddConjuntos))
                                        .addGap(0, 0, Short.MAX_VALUE)))))
                        .addGap(318, 318, 318))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(266, 266, 266)
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFornPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)))
                .addComponent(jLabel6)
                .addGap(136, 136, 136))
            .addGroup(layout.createSequentialGroup()
                .addGap(55, 55, 55)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFornId, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtFornPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel15)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboCnpjCpf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFornCnpjCpf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16)
                    .addComponent(lblCnpjCpf)
                    .addComponent(jLabel7)
                    .addComponent(txtFornId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19)
                    .addComponent(txtDataCadForn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtNomeForn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFornFantasia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(txtFornRepr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(txtFornCep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(txtFornCidade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(cboUf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFornLograd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFornCompl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFornBairro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFornFone1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(jLabel12)
                    .addComponent(txtFornFone2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(txtFornFone3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFornEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                        .addComponent(btnLimpar)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 87, Short.MAX_VALUE)
                .addComponent(txtIdEmpresa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19))
        );

        btnImprimir.getAccessibleContext().setAccessibleDescription("Imprimir Documento de Adesão");
        btnPesquisar.getAccessibleContext().setAccessibleDescription("Pesquisar Por ID");

        setBounds(0, 0, 900, 719);
    }// </editor-fold>//GEN-END:initComponents

    private void txtNomeFornActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNomeFornActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNomeFornActionPerformed

    private void btnAdicionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarActionPerformed
        adicionar();
    }//GEN-LAST:event_btnAdicionarActionPerformed

    private void btnAlterarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlterarActionPerformed
        alterar();
    }//GEN-LAST:event_btnAlterarActionPerformed

    private void txtFornPesquisarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFornPesquisarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFornPesquisarActionPerformed

    private void btnRemoverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoverActionPerformed
        remover();
    }//GEN-LAST:event_btnRemoverActionPerformed

    //metodo do tipo em tempo de execução conforme for informado a informação ele é acionado
    private void txtFornPesquisarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtFornPesquisarKeyReleased
        //Chamar o metodo pesquisa clientes 
        pesquisar_cliente();
    }//GEN-LAST:event_txtFornPesquisarKeyReleased

    //Evento que sera usado para setar os campos da tabela clicando com o botão do mouse
    private void tblFornecedorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblFornecedorMouseClicked
        // Chamando o metodo para setar os campos
        setar_campos();
    }//GEN-LAST:event_tblFornecedorMouseClicked

    private void txtFornIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFornIdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFornIdActionPerformed

    private void txtFornEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFornEmailActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFornEmailActionPerformed

    private void txtFornBairroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFornBairroActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFornBairroActionPerformed

    private void txtFornLogradActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFornLogradActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFornLogradActionPerformed

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        imprimirdoc_adesao();
    }//GEN-LAST:event_btnImprimirActionPerformed

    private void txtFornCnpjCpfKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtFornCnpjCpfKeyPressed

    }//GEN-LAST:event_txtFornCnpjCpfKeyPressed

    private void txtFornCnpjCpfFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtFornCnpjCpfFocusLost

        if (cboCnpjCpf.getSelectedItem().equals("CNPJ")) {
            if (!txtFornCnpjCpf.getText().equals("  .   .   /    -  ")) {
                consultarCNPJCPF();

            }
        } else if (cboCnpjCpf.getSelectedItem().equals("CPF")) {

            if (!txtFornCnpjCpf.getText().equals("   .   .   -  ")) {
                consultarCNPJCPF();

            }
        }


    }//GEN-LAST:event_txtFornCnpjCpfFocusLost

    private void txtFornCepFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtFornCepFocusLost
        if (txtFornCep.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Informe o CEP");
        } else {
            buscarCep();

        }
    }//GEN-LAST:event_txtFornCepFocusLost

    private void cboCnpjCpfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboCnpjCpfActionPerformed

        int tipoDoc = cboCnpjCpf.getSelectedIndex();

        switch (tipoDoc) {
            case 0:
                txtFornCnpjCpf.setFormatterFactory(new DefaultFormatterFactory(CNPJMask));
                lblCnpjCpf.setText("CNPJ");
                break;
            case 1:
                txtFornCnpjCpf.setFormatterFactory(new DefaultFormatterFactory(CPFMask));
                lblCnpjCpf.setText("CPF");
                break;
        }


    }//GEN-LAST:event_cboCnpjCpfActionPerformed

    private void txtFornComplActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFornComplActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFornComplActionPerformed

    private void txtFornReprActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFornReprActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFornReprActionPerformed

    private void btnPesquisarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPesquisarActionPerformed
        consultar();
    }//GEN-LAST:event_btnPesquisarActionPerformed

    private void txtFornCnpjCpfFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtFornCnpjCpfFocusGained


    }//GEN-LAST:event_txtFornCnpjCpfFocusGained

    private void btnLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparActionPerformed
        limpar();
        btnImprimir.setEnabled(false);
    }//GEN-LAST:event_btnLimparActionPerformed

    private void txtDataCadFornActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDataCadFornActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDataCadFornActionPerformed

    private void btnAddConjuntosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddConjuntosActionPerformed
        TelaVeiculo veiculo = new TelaVeiculo();
        getParent().add(veiculo);
        veiculo.setVisible(true);
        dispose();
    }//GEN-LAST:event_btnAddConjuntosActionPerformed

    private void txtFornPesquisarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtFornPesquisarMouseClicked
        pesquisar_cliente();
    }//GEN-LAST:event_txtFornPesquisarMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddConjuntos;
    private javax.swing.JButton btnAdicionar;
    private javax.swing.JButton btnAlterar;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnLimpar;
    private javax.swing.JButton btnPesquisar;
    private javax.swing.JButton btnRemover;
    private javax.swing.JComboBox<String> cboCnpjCpf;
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
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCnpjCpf;
    private javax.swing.JTable tblFornecedor;
    private javax.swing.JTextField txtDataCadForn;
    private javax.swing.JTextField txtFornBairro;
    private javax.swing.JFormattedTextField txtFornCep;
    private javax.swing.JTextField txtFornCidade;
    private javax.swing.JFormattedTextField txtFornCnpjCpf;
    private javax.swing.JTextField txtFornCompl;
    private javax.swing.JTextField txtFornEmail;
    private javax.swing.JTextField txtFornFantasia;
    private javax.swing.JTextField txtFornFone1;
    private javax.swing.JTextField txtFornFone2;
    private javax.swing.JTextField txtFornFone3;
    public static javax.swing.JTextField txtFornId;
    private javax.swing.JTextField txtFornLograd;
    private javax.swing.JTextField txtFornPesquisar;
    private javax.swing.JTextField txtFornRepr;
    private javax.swing.JTextField txtIdEmpresa;
    public static javax.swing.JTextField txtNomeForn;
    // End of variables declaration//GEN-END:variables
}
