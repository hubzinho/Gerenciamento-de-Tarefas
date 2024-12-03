package GerenciadorDeTarefas;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class GerenciadorDeTarefas {
    private static Connection conectarBancoDeDados() {
        try {
            // Conectar ao banco de dados SQLite
            return DriverManager.getConnection("jdbc:sqlite:tarefas.db");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao conectar ao banco de dados: " + e.getMessage());
            return null;
        }
    }

    private static void criarTabelaNoBanco(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Criar tabela "tarefas" se não existir
            String sql = """
                    CREATE TABLE IF NOT EXISTS tarefas (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        tarefa TEXT NOT NULL,
                        descricao TEXT,
                        prazo TEXT,
                        prioridade TEXT,
                        concluida INTEGER
                    );
                    """;
            stmt.execute(sql);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao criar tabela no banco: " + e.getMessage());
        }
    }

    private static void preencherTabela(JTable tabela, Connection conn) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT tarefa, descricao, prazo, prioridade, concluida FROM tarefas")) {

            DefaultTableModel modelo = (DefaultTableModel) tabela.getModel();
            modelo.setRowCount(0); // Limpar a tabela

            while (rs.next()) {
                String tarefa = rs.getString("tarefa");
                String descricao = rs.getString("descricao");
                String prazo = rs.getString("prazo");
                String prioridade = rs.getString("prioridade");
                String concluida = rs.getInt("concluida") == 1 ? "Sim" : "Não";

                modelo.addRow(new Object[]{tarefa, descricao, prazo, prioridade, concluida});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao preencher a tabela: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Conexão com o banco de dados
        Connection conn = conectarBancoDeDados();
        if (conn == null) return;
        criarTabelaNoBanco(conn);

        // Criar a janela principal
        JFrame frame = new JFrame("Gerenciador de Tarefas");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLayout(null);

        // Campo de Tarefa
        JLabel labelTarefa = new JLabel("Tarefa:");
        labelTarefa.setBounds(30, 30, 100, 25);
        JTextField campoTarefa = new JTextField();
        campoTarefa.setBounds(150, 30, 300, 25);
        frame.add(labelTarefa);
        frame.add(campoTarefa);

        // Campo de Descrição
        JLabel labelDescricao = new JLabel("Descrição (Opcional):");
        labelDescricao.setBounds(30, 70, 150, 25);
        JTextField campoDescricao = new JTextField();
        campoDescricao.setBounds(150, 70, 300, 25);
        frame.add(labelDescricao);
        frame.add(campoDescricao);

        // Campo de Prazo
        JLabel labelPrazo = new JLabel("Prazo (Opcional):");
        labelPrazo.setBounds(30, 110, 150, 25);
        JTextField campoPrazo = new JTextField();
        campoPrazo.setBounds(150, 110, 300, 25);
        frame.add(labelPrazo);
        frame.add(campoPrazo);

        // Prioridade
        JLabel labelPrioridade = new JLabel("Prioridade:");
        labelPrioridade.setBounds(30, 150, 100, 25);
        JRadioButton baixa = new JRadioButton("Baixa");
        baixa.setBounds(150, 150, 70, 25);
        JRadioButton media = new JRadioButton("Média");
        media.setBounds(220, 150, 70, 25);
        JRadioButton alta = new JRadioButton("Alta");
        alta.setBounds(290, 150, 70, 25);
        ButtonGroup grupoPrioridade = new ButtonGroup();
        grupoPrioridade.add(baixa);
        grupoPrioridade.add(media);
        grupoPrioridade.add(alta);
        frame.add(labelPrioridade);
        frame.add(baixa);
        frame.add(media);
        frame.add(alta);

        // Tarefa Concluída
        JCheckBox concluida = new JCheckBox("Tarefa Concluída");
        concluida.setBounds(30, 190, 150, 25);
        frame.add(concluida);

        // Botão de Cadastrar
        JButton botaoCadastrar = new JButton("Cadastrar");
        botaoCadastrar.setBounds(200, 230, 100, 25);
        frame.add(botaoCadastrar);

        // Tabela para exibir tarefas
        String[] colunas = {"Tarefa", "Descrição", "Prazo", "Prioridade", "Concluída"};
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0);
        JTable tabela = new JTable(modelo);
        JScrollPane scrollPane = new JScrollPane(tabela);
        scrollPane.setBounds(30, 280, 520, 250);
        frame.add(scrollPane);

        // Preencher tabela ao iniciar
        preencherTabela(tabela, conn);

        // Configuração do botão cadastrar
        botaoCadastrar.addActionListener(e -> {
            String tarefa = campoTarefa.getText().trim();
            String descricao = campoDescricao.getText();
            String prazo = campoPrazo.getText();
            String prioridade = baixa.isSelected() ? "Baixa" : media.isSelected() ? "Média" : alta.isSelected() ? "Alta" : "Nenhuma";
            boolean estaConcluida = concluida.isSelected();

            if (tarefa.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "O campo 'Tarefa' é obrigatório!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO tarefas (tarefa, descricao, prazo, prioridade, concluida) VALUES (?, ?, ?, ?, ?)")) {
                pstmt.setString(1, tarefa);
                pstmt.setString(2, descricao);
                pstmt.setString(3, prazo);
                pstmt.setString(4, prioridade);
                pstmt.setInt(5, estaConcluida ? 1 : 0);
                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(frame, "Tarefa cadastrada com sucesso!");
                preencherTabela(tabela, conn); // Atualizar tabela
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Erro ao cadastrar tarefa: " + ex.getMessage());
            }
        });

        // Exibir a janela
        frame.setVisible(true);
    }
}