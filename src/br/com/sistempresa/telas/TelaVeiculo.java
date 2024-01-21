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

public class TelaVeiculo extends javax.swing.JInternalFrame {

    //Usando a variavel de conexao do DAL
    Connection conexao = null;
    //Criando variaveis especiais para conexao com o banco 
    //Prepared Stetament e ResultSet são Frameworks do pacote java.sql
    // e servem para preparar e executar as instruções SQL
    PreparedStatement pst = null;
    ResultSet rs = null;

    private MaskFormatter RenavanMask;
    public int numconjuntos, conjcadastrados;

    /**
     * Creates new form TelaCliente
     */
    public TelaVeiculo() {
        initComponents();
        conexao = ModuloConexao.conector();

        try {
            RenavanMask = new MaskFormatter("###########");

        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        //txtRenavanVeic.setFormatterFactory(new DefaultFormatterFactory(RenavanMask));
        txtRenavanVeic.setDocument(new LimitaCaracteres(60, LimitaCaracteres.TipoEntrada.NUMEROINTEIRO));

        setarIdEmpresa();

        txtIdEmpresa.setVisible(false);
        txtConjCadastradosVeic.setVisible(false);

        txtNomeAssocVeic.setText(TelaAssociado.txtNomeAssoc.getText());

        txtIdAssoc.setText(TelaAssociado.txtIdAssoc.getText());
        txtNumConjVeic.setText(TelaAssociado.cboNumConjAssoc.getSelectedItem().toString());

        setarNumeroDeConj();

        if (Integer.parseInt(txtConjCadastradosVeic.getText()) == Integer.parseInt(txtNumConjVeic.getText())) {

            btnAdicionar.setEnabled(false);
            btnAlterar.setEnabled(true);
            btnRemover.setEnabled(true);
            btnPesquisar.setEnabled(true);
            btnImprimir.setEnabled(true);
        } else {
            btnAdicionar.setEnabled(true);
            btnAlterar.setEnabled(false);
            btnRemover.setEnabled(false);
            btnPesquisar.setEnabled(true);
            btnImprimir.setEnabled(false);
        }

    }

    private void adicionar() {

        if (Integer.parseInt(txtConjCadastradosVeic.getText()) == (Integer.parseInt(txtNumConjVeic.getText()) - 1)) {

            String sql = "insert into tbveiculo(nomepropr, marca, modelo, ano, placa, renavan, "
                    + "idassoc, numcarretaveic, numdollyveic, idempr, veiculoexcluido) values(?,?,?,?,?,?,?,?,?,?,?)";

            try {
                pst = conexao.prepareStatement(sql);

                pst.setString(1, txtNomeAssocVeic.getText());
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

                if ((txtNomeAssocVeic.getText().isEmpty()) || (txtRenavanVeic.getText().isEmpty()) || (txtPlacaVeic.getText().isEmpty())) {
                    JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatorios.");

                } else {
                    int adicionado = pst.executeUpdate();

                    //System.out.println(adicionado);
                    if (adicionado > 0) {

                        JOptionPane.showMessageDialog(null, "Veiculo adicionado com sucesso");

                        setarNumeroDeConj();

                        limpar();

                        btnAdicionar.setEnabled(false);
                        btnAlterar.setEnabled(true);
                        btnRemover.setEnabled(true);
                        btnPesquisar.setEnabled(true);
                        btnImprimir.setEnabled(true);

                    }
                }

                //for (int numconjuntos = Integer.parseInt(txtNumConjVeic.getText()); numconjuntos > 1; numconjuntos--) {
                //Validação dos campos obrigatorios
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }

        } else {

            String sql = "insert into tbveiculo(nomepropr, marca, modelo, ano, placa, renavan, "
                    + "idassoc, numcarretaveic, numdollyveic, idempr, veiculoexcluido) values(?,?,?,?,?,?,?,?,?,?,?)";

            try {
                pst = conexao.prepareStatement(sql);

                pst.setString(1, txtNomeAssocVeic.getText());
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

                if ((txtNomeAssocVeic.getText().isEmpty()) || (txtRenavanVeic.getText().isEmpty()) || (txtPlacaVeic.getText().isEmpty())) {
                    JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatorios.");

                } else {
                    int adicionado = pst.executeUpdate();

                    //System.out.println(adicionado);
                    if (adicionado > 0) {

                        JOptionPane.showMessageDialog(null, "Veiculo adicionado com sucesso");

                        setarNumeroDeConj();

                        limpar();

                    }
                }
                //for (int numconjuntos = Integer.parseInt(txtNumConjVeic.getText()); numconjuntos > 1; numconjuntos--) {
                //Validação dos campos obrigatorios
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }

    }

    //PEsquisa cliente pelo nome
    private void pesquisar_veiculo() {

        String sql = "select idveic as ID, nomepropr as Associado, marca as Marca, modelo as Modelo, ano as Ano, placa AS Placa, renavan AS Renavan from tbveiculo where nomepropr like ?";

        try {
            pst = conexao.prepareStatement(sql);
            //Passando o conteudo da caixa de pesquisa para o ?
            //atenção ao "%" - continuação da pesquisa sql
            pst.setString(1, txtPesquisarVeic.getText() + "%");
            rs = pst.executeQuery();
            // a linha abaixo usa a biblioteca rs2xml.jar para preencher a tabela 
            tblVeiculos.setModel(DbUtils.resultSetToTableModel(rs));

            tblVeiculos.getColumnModel().getColumn(0).setMaxWidth(50);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    //Metodo para setar os campos do formulario com o conteudo da tabela    
    public void setar_campos() {

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
                txtNomeAssocVeic.setText(rs.getString(3));
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

                btnAdicionar.setEnabled(true);
                btnAlterar.setEnabled(true);
                btnRemover.setEnabled(true);
                btnPesquisar.setEnabled(true);
                btnImprimir.setEnabled(true);

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

        String id_veic = JOptionPane.showInputDialog("Informe o ID do Veiculo?");

        String sql = "select idveic, date_format(data_cadveic,'%d/%m/%Y'), nomepropr, marca, modelo, ano, placa, renavan, idassoc, numcarretaveic, numdollyveic, idempr from tbveiculo where idveic = " + id_veic;

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {

                txtIdVeic.setText(rs.getString(1));
                txtDataCadVeic.setText(rs.getString(2));
                txtNomeAssocVeic.setText(rs.getString(3));
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

    private void alterar() {

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
        String sql = "update tbveiculo set nomepropr=?, marca=?, modelo=?, ano=?, placa=?, renavan=?, idassoc=?, numcarretaveic=?, numdollyveic=?, idempr=? where idveic = ?";

        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, txtNomeAssocVeic.getText());
            pst.setString(2, txtMarcaVeic.getText());
            pst.setString(3, txtModeloVeic.getText());
            pst.setString(4, txtAnoVeic.getText());
            pst.setString(5, txtPlacaVeic.getText());
            pst.setString(6, txtRenavanVeic.getText());
            pst.setString(7, txtIdAssoc.getText());
            pst.setString(8, cboNumCarretaVeic.getSelectedItem().toString());
            pst.setString(9, cboNumDollyVeic.getSelectedItem().toString());
            pst.setString(10, txtIdEmpresa.getText());
            if (Integer.parseInt(txtConjCadastradosVeic.getText()) < (Integer.parseInt(txtNumConjVeic.getText()))) {
                pst.setString(11, String.valueOf(statusnum));
            } else {
                pst.setString(11,"0");
            }
            pst.setString(12, txtIdVeic.getText());

            if ((txtNomeAssocVeic.getText().isEmpty()) || (txtMarcaVeic.getText().isEmpty())) {
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

    private void remover() {
        int confirma = JOptionPane.showConfirmDialog(null, "Tem Certeza que deseja excluir este veiculo?", "Atenção", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {
            
            String sql = "update tbveiculo set veiculoexcluido = 1 where idveic=?";
            
            try {
                pst = conexao.prepareStatement(sql);
                pst.setString(1, txtIdVeic.getText());
                int apagado = pst.executeUpdate();

                if (apagado > 0) {
                    JOptionPane.showMessageDialog(null, "Veiculo removido com sucesso");

                    limpar();

                    btnAdicionar.setEnabled(true);
                    btnPesquisar.setEnabled(true);
                    btnAlterar.setEnabled(false);
                    btnRemover.setEnabled(false);
                    btnImprimir.setEnabled(false);

                    setarNumeroDeConj();
                    pesquisar_veiculo();

                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    public void limpar() {
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

        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(false);
        btnRemover.setEnabled(false);

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

    public void setarNumeroDeConj() {

        TelaAssociado associado = new TelaAssociado();

        String id_assoc = txtIdAssoc.getText();
        String id_empr = txtIdEmpresa.getText();
        String statusveiculo = String.valueOf(0);

        String sql = "SELECT COUNT(*) FROM  tbveiculo where idassoc = " + id_assoc + " and idempr = " + id_empr + " and veiculoexcluido = 0";

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                txtConjCadastradosVeic.setText(rs.getString(1));

            }
        } catch (SQLException e2) {
            JOptionPane.showMessageDialog(null, e2);
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelasoc = new javax.swing.JLabel();
        txtNomeAssocVeic = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtMarcaVeic = new javax.swing.JTextField();
        btnAdicionar = new javax.swing.JButton();
        btnAlterar = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        txtPesquisarVeic = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        btnRemover = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        txtIdVeic = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        cboNumCarretaVeic = new javax.swing.JComboBox<>();
        btnImprimir = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        txtPlacaVeic = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblVeiculos = new javax.swing.JTable();
        btnPesquisar = new javax.swing.JButton();
        txtDataCadVeic = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        txtIdEmpresa = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        txtRenavanVeic = new javax.swing.JFormattedTextField();
        txtAnoVeic = new javax.swing.JFormattedTextField();
        cboNumDollyVeic = new javax.swing.JComboBox<>();
        jLabel16 = new javax.swing.JLabel();
        txtModeloVeic = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        txtIdAssoc = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        txtNumConjVeic = new javax.swing.JTextField();
        txtConjCadastradosVeic = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        cboStatusVeic = new javax.swing.JComboBox<>();
        lblStatusVeic = new javax.swing.JLabel();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setTitle("Conjuntos");
        setToolTipText("");
        setPreferredSize(new java.awt.Dimension(900, 719));

        jLabelasoc.setText("*Associado");

        txtNomeAssocVeic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNomeAssocVeicActionPerformed(evt);
            }
        });

        jLabel2.setText("Marca");

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

        txtPesquisarVeic.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtPesquisarVeicMouseClicked(evt);
            }
        });
        txtPesquisarVeic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPesquisarVeicActionPerformed(evt);
            }
        });
        txtPesquisarVeic.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtPesquisarVeicKeyReleased(evt);
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

        txtIdVeic.setEnabled(false);
        txtIdVeic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdVeicActionPerformed(evt);
            }
        });

        jLabel8.setText("Nº Doly");

        jLabel10.setText("Nº de Carretas");

        cboNumCarretaVeic.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", " " }));

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

        jLabel15.setText("Pesquisar Associado");

        jLabel18.setText("Placa");

        txtPlacaVeic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPlacaVeicActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Conjuntos do Associado"));

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
        jScrollPane1.setViewportView(tblVeiculos);

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

        txtDataCadVeic.setEnabled(false);
        txtDataCadVeic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDataCadVeicActionPerformed(evt);
            }
        });

        jLabel19.setText("Data:");

        jLabel20.setText("RENAVAN");

        jLabel21.setText("Ano");

        txtRenavanVeic.setToolTipText("");
        txtRenavanVeic.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtRenavanVeicFocusLost(evt);
            }
        });

        txtAnoVeic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAnoVeicActionPerformed(evt);
            }
        });

        cboNumDollyVeic.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", " ", " " }));

        jLabel16.setText("Modelo");

        jLabel22.setText("Id do Associado");

        txtIdAssoc.setEnabled(false);
        txtIdAssoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdAssocActionPerformed(evt);
            }
        });

        jLabel1.setText("Nº de Conj");

        txtNumConjVeic.setEnabled(false);

        jLabel3.setText("Status");

        cboStatusVeic.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Ativo", "Inativo", " " }));

        lblStatusVeic.setText("status2");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(jLabel5))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(txtConjCadastradosVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(50, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelasoc, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel18, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel20, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtNomeAssocVeic)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtMarcaVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel16)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtModeloVeic))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtRenavanVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtPlacaVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel21)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtAnoVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(cboNumCarretaVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel8)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cboNumDollyVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 505, Short.MAX_VALUE)))
                        .addGap(65, 65, 65))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtIdEmpresa, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(61, 61, 61)
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtIdVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel19)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtDataCadVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel22)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtIdAssoc, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtNumConjVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(28, 28, 28)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cboStatusVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(217, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel15)
                                .addGap(18, 18, 18)
                                .addComponent(txtPesquisarVeic, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel6)
                                .addGap(92, 92, 92))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnRemover, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(199, 199, 199))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblStatusVeic)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtPesquisarVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel15))
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtIdVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19)
                    .addComponent(txtDataCadVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22)
                    .addComponent(txtIdAssoc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(txtNumConjVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(cboStatusVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelasoc)
                    .addComponent(txtNomeAssocVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMarcaVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(txtModeloVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(txtPlacaVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21)
                    .addComponent(txtAnoVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel20)
                    .addComponent(txtRenavanVeic, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel8)
                    .addComponent(cboNumCarretaVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboNumDollyVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemover, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(txtIdEmpresa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtConjCadastradosVeic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblStatusVeic)
                .addGap(98, 98, 98))
        );

        btnImprimir.getAccessibleContext().setAccessibleDescription("Imprimir Documento de Adesão");
        btnPesquisar.getAccessibleContext().setAccessibleDescription("Pesquisar Por ID");

        setBounds(0, 0, 900, 719);
    }// </editor-fold>//GEN-END:initComponents

    private void txtNomeAssocVeicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNomeAssocVeicActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNomeAssocVeicActionPerformed

    private void btnAdicionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarActionPerformed
        adicionar();
    }//GEN-LAST:event_btnAdicionarActionPerformed

    private void btnAlterarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlterarActionPerformed
        alterar();
    }//GEN-LAST:event_btnAlterarActionPerformed

    private void txtPesquisarVeicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPesquisarVeicActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPesquisarVeicActionPerformed

    private void btnRemoverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoverActionPerformed
        remover();
    }//GEN-LAST:event_btnRemoverActionPerformed

    //metodo do tipo em tempo de execução conforme for informado a informação ele é acionado
    private void txtPesquisarVeicKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPesquisarVeicKeyReleased
        //Chamar o metodo pesquisa clientes 
        pesquisar_veiculo();
    }//GEN-LAST:event_txtPesquisarVeicKeyReleased

    //Evento que sera usado para setar os campos da tabela clicando com o botão do mouse
    private void tblVeiculosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblVeiculosMouseClicked
        // Chamando o metodo para setar os campos
        setar_campos();
    }//GEN-LAST:event_tblVeiculosMouseClicked

    private void txtIdVeicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdVeicActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIdVeicActionPerformed

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        imprimirdoc_adesao_do_veic();
    }//GEN-LAST:event_btnImprimirActionPerformed

    private void txtPlacaVeicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPlacaVeicActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPlacaVeicActionPerformed

    private void btnPesquisarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPesquisarActionPerformed
        consultar();
    }//GEN-LAST:event_btnPesquisarActionPerformed

    private void txtDataCadVeicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDataCadVeicActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDataCadVeicActionPerformed

    private void txtAnoVeicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAnoVeicActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAnoVeicActionPerformed

    private void txtIdAssocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdAssocActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIdAssocActionPerformed

    private void txtPesquisarVeicMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtPesquisarVeicMouseClicked
        pesquisar_veiculo();
    }//GEN-LAST:event_txtPesquisarVeicMouseClicked

    private void txtRenavanVeicFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtRenavanVeicFocusLost
        renavanJaCadast();
    }//GEN-LAST:event_txtRenavanVeicFocusLost


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionar;
    private javax.swing.JButton btnAlterar;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnPesquisar;
    private javax.swing.JButton btnRemover;
    private javax.swing.JComboBox<String> cboNumCarretaVeic;
    private javax.swing.JComboBox<String> cboNumDollyVeic;
    private javax.swing.JComboBox<String> cboStatusVeic;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabelasoc;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblStatusVeic;
    private javax.swing.JTable tblVeiculos;
    private javax.swing.JFormattedTextField txtAnoVeic;
    private javax.swing.JTextField txtConjCadastradosVeic;
    private javax.swing.JTextField txtDataCadVeic;
    private javax.swing.JTextField txtIdAssoc;
    private javax.swing.JTextField txtIdEmpresa;
    private javax.swing.JTextField txtIdVeic;
    private javax.swing.JTextField txtMarcaVeic;
    private javax.swing.JTextField txtModeloVeic;
    private javax.swing.JTextField txtNomeAssocVeic;
    private javax.swing.JTextField txtNumConjVeic;
    private javax.swing.JTextField txtPesquisarVeic;
    private javax.swing.JTextField txtPlacaVeic;
    private javax.swing.JFormattedTextField txtRenavanVeic;
    // End of variables declaration//GEN-END:variables
}
