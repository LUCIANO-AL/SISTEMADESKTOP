/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.sistempresa.telas;

import br.com.sistempresa.dal.ModuloConexao;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLSyntaxErrorException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author Luciano & Paty
 */
public class TelaEmpresa extends javax.swing.JInternalFrame {

    //Usando a variavel de conexao do DAL
    Connection conexao = null;
    //Criando variaveis especiais para conexao com o banco 
    //Prepared Stetament e ResultSet são Frameworks do pacote java.sql
    // e servem para preparar e executar as instruções SQL
    PreparedStatement pst = null;
    ResultSet rs = null;

    //Instanciar objeto para o fluxo de bytes
    private FileInputStream fis;

    //variável global para armazenar o tamanho da imagen(bytes)
    private int tamanho;
    private MaskFormatter CNPJMask, CEPMask;

    /**
     * Creates new form TelaEmpresa
     */
    public TelaEmpresa() {
        initComponents();
        conexao = ModuloConexao.conector();

        try {
            CNPJMask = new MaskFormatter("##.###.###/####-##");
            CEPMask = new MaskFormatter("##.###-###");
        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        txtCepEmpr.setFormatterFactory(new DefaultFormatterFactory(CEPMask));
        txtCnpjEmpr.setFormatterFactory(new DefaultFormatterFactory(CNPJMask));
    }

    private void adicionar() {

        String sql = "insert into tbempresa(nomeempr, logo, nomefanteempr, ufempr, cidadeempr, bairroempr, logradouroempr, complempr, cepempr, fone1empr, fone2empr, fone3empr, emailempr, cnpjempr) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            pst = conexao.prepareStatement(sql);

            pst.setString(1, txtNomeEmpr.getText());
            pst.setBlob(2, fis, tamanho);
            pst.setString(3, txtFantasiaEmpr.getText());
            pst.setString(4, cboUfEmpr.getSelectedItem().toString());
            pst.setString(5, txtCidadeEmpr.getText());
            pst.setString(6, txtBairroEmpr.getText());
            pst.setString(7, txtLogradEmpr.getText());
            pst.setString(8, txtComplEmpr.getText());
            pst.setString(9, txtCepEmpr.getText());
            pst.setString(10, txtFone1Empr.getText());
            pst.setString(11, txtFone2Empr.getText());
            pst.setString(12, txtFone3Empr.getText());
            pst.setString(13, txtEmailEmpr.getText());
            pst.setString(14, txtCnpjEmpr.getText());

            //Validação dos campos obrigatorios
            if ((txtNomeEmpr.getText().isEmpty()) || (txtCnpjEmpr.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatorios.");

            } else {

                int adicionado = pst.executeUpdate();

                //System.out.println(adicionado);
                if (adicionado > 0) {

                    JOptionPane.showMessageDialog(null, "Empresa adicionado com sucesso");

                    limpar();
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void consultar() {

        String id_assoc = JOptionPane.showInputDialog("Informe o ID da Empresa?");

        String sql = "select idempr, date_format(datacadastempr,'%d/%m/%Y'), nomeempr, logo, nomefanteempr, ufempr, cidadeempr, bairroempr, logradouroempr, complempr, cepempr, fone1empr, fone2empr, fone3empr, emailempr, cnpjempr from tbempresa where idempr =" + id_assoc;

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                txtIdEmpr.setText(rs.getString(1));
                txtDataEmpr.setText(rs.getString(2));
                txtNomeEmpr.setText(rs.getString(3));
                
                Blob blob = (Blob) rs.getBlob(4);
                byte[] img = blob.getBytes(1, (int) blob.length());
                BufferedImage imagem = null;
                try {
                    imagem = ImageIO.read(new ByteArrayInputStream(img));                    
                } catch (Exception e) {
                     JOptionPane.showMessageDialog(null, e);
                }
                
                ImageIcon icone = new ImageIcon(imagem);
                Icon foto = new ImageIcon(icone.getImage().getScaledInstance(lblLogo.getWidth(), lblLogo.getHeight(), Image.SCALE_SMOOTH));
                lblLogo.setIcon(foto);
                
                txtFantasiaEmpr.setText(rs.getString(5));
                cboUfEmpr.setSelectedItem(rs.getString(6));
                txtCidadeEmpr.setText(rs.getString(7));
                txtBairroEmpr.setText(rs.getString(8));
                txtLogradEmpr.setText(rs.getString(9));
                txtComplEmpr.setText(rs.getString(10));
                txtCepEmpr.setText(rs.getString(11));
                txtFone1Empr.setText(rs.getString(12));
                txtFone2Empr.setText(rs.getString(13));
                txtFone3Empr.setText(rs.getString(14));
                txtEmailEmpr.setText(rs.getString(15));
                txtCnpjEmpr.setText(rs.getString(16));

                btnAdicionar.setEnabled(false);
                btnAlterar.setEnabled(true);
                
                btnPesquisar.setEnabled(true);

            } else {
                JOptionPane.showMessageDialog(null, "Empresa não cadastrada.");
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

        String sql = "update tbempresa set nomeempr=?, logo=?, nomefanteempr=?, ufempr=?, cidadeempr=?, bairroempr=?, logradouroempr=?, complempr=?, cepempr=?, fone1empr=?, fone2empr=?, fone3empr=?, emailempr=?, cnpjempr=? where idempr = ?";

        try {

            pst = conexao.prepareStatement(sql);           
            
            pst.setString(1, txtNomeEmpr.getText());
            pst.setBlob(2, fis, tamanho);
            pst.setString(3, txtFantasiaEmpr.getText());
            pst.setString(4, cboUfEmpr.getSelectedItem().toString());
            pst.setString(5, txtCidadeEmpr.getText());
            pst.setString(6, txtBairroEmpr.getText());
            pst.setString(7, txtLogradEmpr.getText());
            pst.setString(8, txtComplEmpr.getText());
            pst.setString(9, txtCepEmpr.getText());
            pst.setString(10, txtFone1Empr.getText());
            pst.setString(11, txtFone2Empr.getText());
            pst.setString(12, txtFone3Empr.getText());
            pst.setString(13, txtEmailEmpr.getText());
            pst.setString(14, txtCnpjEmpr.getText());
            pst.setString(15, txtIdEmpr.getText());

            //Validação dos campos obrigatorios
            if ((txtNomeEmpr.getText().isEmpty()) || (txtFantasiaEmpr.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatorios.");

            } else {
                int adicionado = pst.executeUpdate();

                //System.out.println(adicionado);
                if (adicionado > 0) {

                    JOptionPane.showMessageDialog(null, "Dados da empresa alterados com sucesso");

                    limpar();

                    btnAdicionar.setEnabled(false);
                    btnAlterar.setEnabled(true);
                   
                    btnPesquisar.setEnabled(true);

                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void carregarImg() {
        JFileChooser jfc = new JFileChooser();
        jfc.setDialogTitle("Seleciona imagen");
        jfc.setFileFilter(new FileNameExtensionFilter("Arquivo de imagens(*.PNG,*.JPG,*.JPEG", "png", "jpg", "jpeg"));
        int resultado = jfc.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            try {
                fis = new FileInputStream(jfc.getSelectedFile());
                tamanho = (int) jfc.getSelectedFile().length();
                Image foto = ImageIO.read(jfc.getSelectedFile()).getScaledInstance(lblLogo.getWidth(), lblLogo.getHeight(), Image.SCALE_SMOOTH);
                lblLogo.setIcon(new ImageIcon(foto));
                lblLogo.updateUI();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    public void buscarCNPJ() throws MalformedURLException {

        String cnpjMasc = txtCnpjEmpr.getText();
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

                txtNomeEmpr.setText((String) jSONObject.get("nome"));
                txtFantasiaEmpr.setText((String) jSONObject.get("fantasia"));
                txtCepEmpr.setText((String) jSONObject.get("cep"));
                logradouro = (String) jSONObject.get("logradouro");
                numero = (String) jSONObject.get("numero");
                txtLogradEmpr.setText(logradouro + " Nº " + numero);
                txtComplEmpr.setText((String) jSONObject.get("complemento"));
                txtBairroEmpr.setText((String) jSONObject.get("bairro"));
                txtCidadeEmpr.setText((String) jSONObject.get("municipio"));
                cboUfEmpr.setSelectedItem((String) jSONObject.get("uf"));
                txtFone1Empr.setText((String) jSONObject.get("telefone"));
                txtEmailEmpr.setText((String) jSONObject.get("email"));
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
        txtNomeEmpr.setText(null);
        txtFantasiaEmpr.setText(null);
        txtCidadeEmpr.setText(null);
        txtBairroEmpr.setText(null);
        txtLogradEmpr.setText(null);
        txtComplEmpr.setText(null);
        txtCepEmpr.setText(null);
        txtFone1Empr.setText(null);
        txtFone2Empr.setText(null);
        txtFone3Empr.setText(null);
        txtEmailEmpr.setText(null);
        txtCnpjEmpr.setText(null);
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
        btnPesquisar = new javax.swing.JButton();
        btnAdicionar = new javax.swing.JButton();
        btnAlterar = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        btnAddLogo = new javax.swing.JButton();
        txtBairroEmpr = new javax.swing.JTextField();
        txtCepEmpr = new javax.swing.JFormattedTextField();
        lblCnpjCpf = new javax.swing.JLabel();
        txtCnpjEmpr = new javax.swing.JFormattedTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        txtIdEmpr = new javax.swing.JTextField();
        txtComplEmpr = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtCidadeEmpr = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtFone1Empr = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtFone2Empr = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        txtNomeEmpr = new javax.swing.JTextField();
        txtFone3Empr = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtDataEmpr = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        txtEmailEmpr = new javax.swing.JTextField();
        txtFantasiaEmpr = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        txtLogradEmpr = new javax.swing.JTextField();
        cboUfEmpr = new javax.swing.JComboBox<>();
        jButton1 = new javax.swing.JButton();

        jLabel1.setText("jLabel1");

        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Cadastrar Empresa");
        setPreferredSize(new java.awt.Dimension(900, 719));

        btnPesquisar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/sistempresa/icones/read.png"))); // NOI18N
        btnPesquisar.setToolTipText("Consultar");
        btnPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnPesquisar.setPreferredSize(new java.awt.Dimension(80, 80));
        btnPesquisar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPesquisarActionPerformed(evt);
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

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lblLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/sistempresa/icones/camera.png"))); // NOI18N

        btnAddLogo.setText("Carregar Imagen");
        btnAddLogo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddLogoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(57, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblLogo)
                    .addComponent(btnAddLogo))
                .addGap(53, 53, 53))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(lblLogo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addComponent(btnAddLogo)
                .addContainerGap())
        );

        txtBairroEmpr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBairroEmprActionPerformed(evt);
            }
        });

        txtCepEmpr.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtCepEmprFocusLost(evt);
            }
        });

        lblCnpjCpf.setText("CNPJ");

        txtCnpjEmpr.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtCnpjEmprFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtCnpjEmprFocusLost(evt);
            }
        });
        txtCnpjEmpr.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtCnpjEmprKeyPressed(evt);
            }
        });

        jLabel7.setText("Id");

        jLabel17.setText("Complemento");

        txtIdEmpr.setEnabled(false);
        txtIdEmpr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdEmprActionPerformed(evt);
            }
        });

        txtComplEmpr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtComplEmprActionPerformed(evt);
            }
        });

        jLabel8.setText("Cidade");

        jLabel10.setText("CEP");

        jLabel11.setText("Fone1");

        jLabel4.setText("*Nome");

        jLabel12.setText("Fone2");

        txtNomeEmpr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNomeEmprActionPerformed(evt);
            }
        });

        jLabel5.setText("Fantasia");

        jLabel13.setText("Fone3");

        jLabel6.setText("Endereço");

        jLabel9.setText("Bairro");

        txtDataEmpr.setEnabled(false);
        txtDataEmpr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDataEmprActionPerformed(evt);
            }
        });

        jLabel19.setText("Data:");

        jLabel14.setText("Email");

        txtEmailEmpr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtEmailEmprActionPerformed(evt);
            }
        });

        jLabel15.setText("UF");

        txtLogradEmpr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtLogradEmprActionPerformed(evt);
            }
        });

        cboUfEmpr.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO" }));

        jButton1.setText("Buscar CNPJ");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(51, 51, 51)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblCnpjCpf, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel17, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtNomeEmpr)
                            .addComponent(txtEmailEmpr)
                            .addComponent(txtComplEmpr)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtFantasiaEmpr, javax.swing.GroupLayout.PREFERRED_SIZE, 443, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtBairroEmpr, javax.swing.GroupLayout.PREFERRED_SIZE, 442, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtCnpjEmpr, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(66, 66, 66)
                                        .addComponent(jButton1))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtFone1Empr, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel12)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtFone2Empr, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel13)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtFone3Empr, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtIdEmpr, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel19)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtDataEmpr, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(txtCepEmpr, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(jLabel8)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(txtCidadeEmpr, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel15)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(cboUfEmpr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(txtLogradEmpr, javax.swing.GroupLayout.PREFERRED_SIZE, 443, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 1, Short.MAX_VALUE)))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(249, 249, 249)
                        .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap(60, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(77, 77, 77)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtIdEmpr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDataEmpr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtCnpjEmpr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCnpjCpf)
                            .addComponent(jButton1))
                        .addGap(7, 7, 7)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtNomeEmpr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtFantasiaEmpr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cboUfEmpr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel15)
                                .addComponent(txtCidadeEmpr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel8)
                                .addComponent(txtCepEmpr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtLogradEmpr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtComplEmpr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtBairroEmpr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtFone1Empr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12)
                            .addComponent(txtFone2Empr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13)
                            .addComponent(txtFone3Empr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtEmailEmpr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel14))))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(251, Short.MAX_VALUE))
        );

        setBounds(0, 0, 900, 719);
    }// </editor-fold>//GEN-END:initComponents

    private void btnPesquisarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPesquisarActionPerformed
        consultar();
    }//GEN-LAST:event_btnPesquisarActionPerformed

    private void btnAdicionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarActionPerformed
        adicionar();
    }//GEN-LAST:event_btnAdicionarActionPerformed

    private void btnAlterarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlterarActionPerformed
        alterar();
    }//GEN-LAST:event_btnAlterarActionPerformed

    private void btnAddLogoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddLogoActionPerformed
        carregarImg();
    }//GEN-LAST:event_btnAddLogoActionPerformed

    private void txtBairroEmprActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBairroEmprActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBairroEmprActionPerformed

    private void txtCepEmprFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtCepEmprFocusLost

    }//GEN-LAST:event_txtCepEmprFocusLost

    private void txtCnpjEmprFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtCnpjEmprFocusGained

    }//GEN-LAST:event_txtCnpjEmprFocusGained

    private void txtCnpjEmprFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtCnpjEmprFocusLost

    }//GEN-LAST:event_txtCnpjEmprFocusLost

    private void txtCnpjEmprKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCnpjEmprKeyPressed

    }//GEN-LAST:event_txtCnpjEmprKeyPressed

    private void txtIdEmprActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdEmprActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIdEmprActionPerformed

    private void txtComplEmprActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtComplEmprActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtComplEmprActionPerformed

    private void txtNomeEmprActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNomeEmprActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNomeEmprActionPerformed

    private void txtDataEmprActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDataEmprActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDataEmprActionPerformed

    private void txtEmailEmprActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtEmailEmprActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtEmailEmprActionPerformed

    private void txtLogradEmprActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtLogradEmprActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtLogradEmprActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            buscarCNPJ();
        } catch (MalformedURLException ex) {
            Logger.getLogger(TelaEmpresa.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddLogo;
    private javax.swing.JButton btnAdicionar;
    private javax.swing.JButton btnAlterar;
    private javax.swing.JButton btnPesquisar;
    private javax.swing.JComboBox<String> cboUfEmpr;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblCnpjCpf;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JTextField txtBairroEmpr;
    private javax.swing.JFormattedTextField txtCepEmpr;
    private javax.swing.JTextField txtCidadeEmpr;
    private javax.swing.JFormattedTextField txtCnpjEmpr;
    private javax.swing.JTextField txtComplEmpr;
    private javax.swing.JTextField txtDataEmpr;
    private javax.swing.JTextField txtEmailEmpr;
    private javax.swing.JTextField txtFantasiaEmpr;
    private javax.swing.JTextField txtFone1Empr;
    private javax.swing.JTextField txtFone2Empr;
    private javax.swing.JTextField txtFone3Empr;
    private javax.swing.JTextField txtIdEmpr;
    private javax.swing.JTextField txtLogradEmpr;
    private javax.swing.JTextField txtNomeEmpr;
    // End of variables declaration//GEN-END:variables
}
