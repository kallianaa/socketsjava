package pacote;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Key;

public class ClienteSocketSwing extends JFrame {




    private static final long serialVersionUID = 7293937207942301990L;
    private JTextArea taEditor = new JTextArea("Digite sua pergunta: ");
    private JTextArea taVisor = new JTextArea();
    private JList listaUsuario = new JList();
    private PrintWriter escritor;
    private BufferedReader leitor;
    private JScrollPane scrollTaVisor = new JScrollPane (taVisor);

    public ClienteSocketSwing() {

        setTitle("Chat com Sockets - chatbot.com");
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        listaUsuario.setBackground(Color.lightGray);
        taEditor.setBackground(Color.GRAY);

        taEditor.setPreferredSize(new Dimension(400,250));
        //taVisor.setPreferredSize(new Dimension(400,250));
        taVisor.setEditable(false);
        listaUsuario.setPreferredSize(new Dimension(160,100));

        add(taEditor, BorderLayout.SOUTH);
        add(scrollTaVisor, BorderLayout.CENTER);
        add(new JScrollPane(listaUsuario), BorderLayout.WEST);

        pack();
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        String[] usuarios = new String[]{};
        preencherListaUsuarios(usuarios);
        
    }

    private void preencherListaUsuarios(String[] usuarios) {
        DefaultListModel modelo = new DefaultListModel<>();
        listaUsuario.setModel(modelo);
        for(String usuario: usuarios){
            modelo.addElement(usuario);
        }
        //listaUsuarios.getModel().
    }



    private void iniciarEscritor() {
        taEditor.addKeyListener(new KeyListener() {


            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                //taEditor.getText();
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    //escrevendo para o servidor
                    if(taVisor.getText().isEmpty()){
                        return;
                    }

                    Object usuario = listaUsuario.getSelectedValue();
                    if (usuario != null){
                        //jogando no visor
                        taVisor.append("\n");
                        taVisor.append(taEditor.getText());
                        taVisor.append("\n");

                        escritor.println(Comandos.MENSAGEM + usuario);
                        escritor.println(taEditor.getText());

                        //limpando o editor
                        taEditor.setText("");
                        e.consume();

                    }else{
                        if (taVisor.getText().equalsIgnoreCase(Comandos.SAIR)){
                            System.exit(0);
                        }
                        JOptionPane.showMessageDialog(ClienteSocketSwing.this, "Selecione um usuario");
                        return;
                    }

                }
            }
        });

    }

    public void inicarChat() {
        try {
           final Socket cliente = new Socket("localhost", 8080);
            escritor = new PrintWriter(cliente.getOutputStream(), true);
            leitor = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
        } catch (UnknownHostException e) {
            System.out.println("o endereço passado é invalido");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("o servidor esta fora do ar");
            e.printStackTrace();
        }

    }
    public static void main(String[] args) {
        ClienteSocketSwing cliente = new ClienteSocketSwing();
        cliente.inicarChat();
        cliente.iniciarEscritor();
        cliente.iniciaLeitor();
        cliente.atualizarListaUsuarios();
    }

    private void atualizarListaUsuarios() {
        escritor.println(Comandos.LISTA_USUARIOS);
    }



    private void iniciaLeitor() {
        //lendo mensagens do servidor
        try {
            while (true){
                String mensagem = leitor.readLine();
                if (mensagem == null || mensagem.isEmpty()) {
                    continue;
                }
                //recebe o texto
                if(mensagem.equals(Comandos.LISTA_USUARIOS)){
                    String[] usuarios = leitor.readLine().split(",");
                    preencherListaUsuarios(usuarios);
                }else if(mensagem.equals(Comandos.LOGIN)){
                    String login = JOptionPane.showInputDialog("Qual o seu login? ");
                    escritor.println(login);
                }else if(mensagem.equals(Comandos.LOGIN_NEGADO)){
                    JOptionPane.showMessageDialog(ClienteSocketSwing.this, "O login é inválido!");
                }else if (mensagem.equals(Comandos.LOGIN_ACEITO)){
                    atualizarListaUsuarios();
                }else{
                    taVisor.append(mensagem);
                    taVisor.append("\n");
                    taVisor.setCaretPosition(taVisor.getDocument().getLength());
                }

            }

        } catch (IOException e) {
            System.out.println("impossivel ler a mensagem do servidor");
            e.printStackTrace();
        }
    }

    private DefaultListModel getListaUsuarios() {
        return (DefaultListModel) listaUsuario.getModel();
    }
}
