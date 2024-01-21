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
import java.sql.*;
import br.com.sistempresa.dal.ModuloConexao;
import com.sun.xml.internal.ws.client.ContentNegotiation;
import java.text.ParseException;
import java.util.HashMap;
import javax.swing.JOptionPane;
import javax.swing.text.MaskFormatter;
import net.proteanit.sql.DbUtils;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

public class TelaBanco extends javax.swing.JInternalFrame {

    //Usando a variavel de conexao do DAL
    Connection conexao = null;
    //Criando variaveis especiais para conexao com o banco 
    //Prepared Stetament e ResultSet são Frameworks do pacote java.sql
    // e servem para preparar e executar as instruções SQL
    PreparedStatement pst = null;
    ResultSet rs = null;
       
    private MaskFormatter juros, multa;

    public TelaBanco() {
        initComponents();
        conexao = ModuloConexao.conector();
        preencher_tblusuario();
        
         try {
            juros = new MaskFormatter("##");
            multa = new MaskFormatter("##");
            
        } catch (ParseException ex) {
            ex.printStackTrace();
        }

    }

    private void consultar() {

        String id_banco = JOptionPane.showInputDialog("Id do Banco?");

        String sql = "select * from tbbancos where idbanco = " + id_banco;

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, txtUsuId.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                txtIdBanco.setText(rs.getString(1));
                txtNomeBanco.setText(rs.getString(2));
                txtMultaBanco.setText(rs.getString(3));
                txtJurosBanco.setText(rs.getString(4));

            } else {
                JOptionPane.showMessageDialog(null, "Banco não cadastrado.");
                txtNomeBanco.setText(null);
                txtMultaBanco.setText(null);
                txtJurosBanco.setText(null);

            }
        } catch (SQLSyntaxErrorException e) {
            JOptionPane.showMessageDialog(null, "ID Inválido");
            //System.out.println(e);
        } catch (Exception e2) {
            JOptionPane.showMessageDialog(null, e2);
        }
    }

    private void adicionar() {
        String sql = "insert into tbbancos(nomebanco, juros, multa) values(?,?,?)";
        try {
            pst = conexao.prepareStatement(sql);

            pst.setString(1, txtNomeBanco.getText());
            pst.setString(2, txtJurosBanco.getText());
            pst.setString(3, txtMultaBanco.getText());

            //Validação dos campos obrigatorios
            if ((txtNomeBanco.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null, "Preencha o campo obrigatorio.");

            } else {
                txtIdBanco.setText(null);

                int adicionado = pst.executeUpdate();

                //System.out.println(adicionado);
                if (adicionado > 0) {

                    JOptionPane.showMessageDialog(null, "Banco adicionado com sucesso");

                    txtNomeBanco.setText(null);
                    txtMultaBanco.setText(null);
                    txtJurosBanco.setText(null);

                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void alterar() {
        String sql = "update tbbancos set nomebanco=?, juros=?, multa=? where idbanco=?";
        try {
            pst = conexao.prepareStatement(sql);

            pst.setString(1, txtNomeBanco.getText());
            pst.setString(2, txtJurosBanco.getText());
            pst.setString(3, txtMultaBanco.getText());

            //Validação dos campos obrigatorios
            if ((txtNomeBanco.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null, "Preencha o campo obrigatorio.");

            } else {
                txtIdBanco.setText(null);

                int adicionado = pst.executeUpdate();

                //System.out.println(adicionado);
                if (adicionado > 0) {

                    JOptionPane.showMessageDialog(null, "Alterações executadas com sucesso");

                    txtNomeBanco.setText(null);
                    txtMultaBanco.setText(null);
                    txtJurosBanco.setText(null);

                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void remover() {
        int confirma = JOptionPane.showConfirmDialog(null, "Tem Certeza que deseja excluir este banco", "Atenção", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {
            String sql = "delete from tbbancos where idbanco=?";

            try {
                pst = conexao.prepareStatement(sql);
                pst.setString(1, txtIdBanco.getText());
                int apagado = pst.executeUpdate();

                if (apagado > 0) {
                    JOptionPane.showMessageDialog(null, "Banco removido com sucesso");

                    limpar();
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    private void preencher_tblusuario() {

        String sql = "select idbanco as ID, nomebanco as Banco, juros as Juros, multa as Multa from tbbancos order by idbanco;";

        try {
            pst = conexao.prepareStatement(sql);
            //Passando o conteudo da caixa de pesquisa para o ?
            //atenção ao "%" - continuação da pesquisa sql
            //pst.setString(1, txtCliPesquisar.getText() + "%");
            rs = pst.executeQuery();
            // a linha abaixo usa a biblioteca rs2xml.jar para preencher a tabela 
            tblBanco.setModel(DbUtils.resultSetToTableModel(rs));
            tblBanco.getColumnModel().getColumn(3).setMaxWidth(0);
            tblBanco.getColumnModel().getColumn(3).setMinWidth(0);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    //Metodo para setar os campos do formulario com o conteudo da tabela    
    public void setar_campos() {

        int setar = tblBanco.getSelectedRow();
        txtIdBanco.setText(tblBanco.getModel().getValueAt(setar, 0).toString());
        txtNomeBanco.setText(tblBanco.getModel().getValueAt(setar, 1).toString());
        txtJurosBanco.setText(tblBanco.getModel().getValueAt(setar, 2).toString());
        txtMultaBanco.setText(tblBanco.getModel().getValueAt(setar, 3).toString());

        //A linha abaixo desabilita o botão adicionar
        btnAdicionarUser.setEnabled(false);
        btnUsuUpdate.setEnabled(true);
        btnUsuDelete.setEnabled(true);
        btnUserImprimir.setEnabled(true);

    }

    private void limpar() {

        txtIdBanco.setText(null);
        txtNomeBanco.setText(null);
        txtMultaBanco.setText(null);
        txtJurosBanco.setText(null);

        btnAdicionarUser.setEnabled(true);
        btnUsuUpdate.setEnabled(false);
        btnUsuDelete.setEnabled(false);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtIdBanco = new javax.swing.JTextField();
        txtNomeBanco = new javax.swing.JTextField();
        btnAdicionarUser = new javax.swing.JButton();
        btnUsuRead = new javax.swing.JButton();
        btnUsuUpdate = new javax.swing.JButton();
        btnUsuDelete = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        btnUserImprimir = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblBanco = new javax.swing.JTable();
        btnUserLimpar = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        txtMultaBanco = new javax.swing.JFormattedTextField();
        txtJurosBanco = new javax.swing.JFormattedTextField();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setTitle("Bancos");
        setPreferredSize(new java.awt.Dimension(644, 499));

        jLabel1.setText("ID");

        jLabel2.setText("Banco*");

        jLabel3.setText("Multa");

        txtIdBanco.setEnabled(false);
        txtIdBanco.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdBancoActionPerformed(evt);
            }
        });

        btnAdicionarUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/sistempresa/icones/create.png"))); // NOI18N
        btnAdicionarUser.setToolTipText("Adicionar Usuário");
        btnAdicionarUser.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAdicionarUser.setPreferredSize(new java.awt.Dimension(80, 80));
        btnAdicionarUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdicionarUserActionPerformed(evt);
            }
        });

        btnUsuRead.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/sistempresa/icones/read.png"))); // NOI18N
        btnUsuRead.setToolTipText("Consultar");
        btnUsuRead.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnUsuRead.setPreferredSize(new java.awt.Dimension(80, 80));
        btnUsuRead.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUsuReadActionPerformed(evt);
            }
        });

        btnUsuUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/sistempresa/icones/update.png"))); // NOI18N
        btnUsuUpdate.setToolTipText("Alterar");
        btnUsuUpdate.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnUsuUpdate.setEnabled(false);
        btnUsuUpdate.setPreferredSize(new java.awt.Dimension(80, 80));
        btnUsuUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUsuUpdateActionPerformed(evt);
            }
        });

        btnUsuDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/sistempresa/icones/delete.png"))); // NOI18N
        btnUsuDelete.setToolTipText("Apagar");
        btnUsuDelete.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnUsuDelete.setEnabled(false);
        btnUsuDelete.setPreferredSize(new java.awt.Dimension(80, 80));
        btnUsuDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUsuDeleteActionPerformed(evt);
            }
        });

        jLabel7.setText("* Campo obrigatório");

        btnUserImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/sistempresa/icones/imprimir.png"))); // NOI18N
        btnUserImprimir.setToolTipText("Imprimir OS");
        btnUserImprimir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnUserImprimir.setPreferredSize(new java.awt.Dimension(80, 80));
        btnUserImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUserImprimirActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Tabela de Usuários"));

        tblBanco = new javax.swing.JTable(){
            public boolean isCellEditable(int rowIndex, int colIndex){
                return false;
            }
        };
        tblBanco.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID", "Banco", "Multa", "Juros"
            }
        ));
        tblBanco.getTableHeader().setReorderingAllowed(false);
        tblBanco.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblBancoMouseClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tblBancoMouseReleased(evt);
            }
        });
        jScrollPane2.setViewportView(tblBanco);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 478, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        btnUserLimpar.setLabel("Limpar Campos");
        btnUserLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUserLimparActionPerformed(evt);
            }
        });

        jLabel6.setText("Juros");

        txtJurosBanco.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtJurosBancoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(72, 72, 72)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 46, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(btnUserLimpar)
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(txtMultaBanco, javax.swing.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
                                        .addComponent(txtIdBanco))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                            .addGap(13, 13, 13)
                                            .addComponent(jLabel6)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(txtJurosBanco, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(jLabel2)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(txtNomeBanco, javax.swing.GroupLayout.PREFERRED_SIZE, 366, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGap(24, 24, 24)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(btnAdicionarUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnUsuRead, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnUsuUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnUsuDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnUserImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel7)
                .addGap(80, 80, 80))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnAdicionarUser, btnUsuDelete, btnUsuRead, btnUsuUpdate});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtIdBanco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(txtNomeBanco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMultaBanco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(txtJurosBanco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnUsuRead, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUsuUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUsuDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAdicionarUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUserImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnUserLimpar)
                .addGap(125, 125, 125))
        );

        btnUserImprimir.getAccessibleContext().setAccessibleDescription("Imprimir Relatorio de Usuários");

        setBounds(0, 0, 644, 405);
    }// </editor-fold>//GEN-END:initComponents

    private void txtIdBancoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdBancoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIdBancoActionPerformed

    private void btnUsuReadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUsuReadActionPerformed
        consultar();
    }//GEN-LAST:event_btnUsuReadActionPerformed

    private void btnAdicionarUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarUserActionPerformed
        adicionar();
        preencher_tblusuario();
    }//GEN-LAST:event_btnAdicionarUserActionPerformed

    private void btnUsuUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUsuUpdateActionPerformed
        alterar();
        preencher_tblusuario();
    }//GEN-LAST:event_btnUsuUpdateActionPerformed

    private void btnUsuDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUsuDeleteActionPerformed
        remover();
        preencher_tblusuario();
    }//GEN-LAST:event_btnUsuDeleteActionPerformed

    private void btnUserImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUserImprimirActionPerformed
        int confirma = JOptionPane.showConfirmDialog(null, "Gerar relatório de Usuários?", "Atenção", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {
            //Imprimir relatorio com o framework JasperReport
            try {
                JasperPrint print = JasperFillManager.fillReport(getClass().getResourceAsStream("/impressao/usuarios.jasper"), null, conexao);
                //a linha abaixo apresenta o relatorio através da classe JasperViewer
                JasperViewer.viewReport(print, false);

            } catch (JRException e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }//GEN-LAST:event_btnUserImprimirActionPerformed

    private void tblBancoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblBancoMouseClicked
        setar_campos();
    }//GEN-LAST:event_tblBancoMouseClicked

    private void tblBancoMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblBancoMouseReleased

    }//GEN-LAST:event_tblBancoMouseReleased

    private void btnUserLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUserLimparActionPerformed
        limpar();
    }//GEN-LAST:event_btnUserLimparActionPerformed

    private void txtJurosBancoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtJurosBancoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtJurosBancoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionarUser;
    private javax.swing.JButton btnUserImprimir;
    private javax.swing.JButton btnUserLimpar;
    private javax.swing.JButton btnUsuDelete;
    private javax.swing.JButton btnUsuRead;
    private javax.swing.JButton btnUsuUpdate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable tblBanco;
    private javax.swing.JTextField txtIdBanco;
    private javax.swing.JFormattedTextField txtJurosBanco;
    private javax.swing.JFormattedTextField txtMultaBanco;
    private javax.swing.JTextField txtNomeBanco;
    // End of variables declaration//GEN-END:variables
}
