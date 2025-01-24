package com.opencvcamera;

import java.awt.*;
import javax.swing.*;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

public class Camera extends JFrame {
	private boolean isCameraActive = true;

    private JLabel cameraScreen = new JLabel();
    private VideoCapture capture = new VideoCapture(0);
    private CascadeClassifier faceDetector = new CascadeClassifier("src/com/opencvcamera/haarcascade_frontalface_default.xml");
    private boolean clicked = false;

    public Camera() {
        setLayout(null);
        cameraScreen.setBounds(500, 100, 600, 480);
        JButton btnCadastro = new JButton("Cadastrar Face");
        JButton btnReconhecer = new JButton("Reconhecer Face");
        btnCadastro.setBounds(620, 600, 120, 40);
        btnReconhecer.setBounds(820, 600, 120, 40);

 

        btnCadastro.addActionListener(e -> loginAdm());
        btnReconhecer.addActionListener(e -> {
            int reconhecida = reconhecerFace(); 
            if(reconhecida==1) {
            	abrirSegundaTela();
            	cameraScreen.setVisible(false);
            	btnCadastro.setVisible(false);
            	btnReconhecer.setVisible(false);
            }
        });
        
        add(cameraScreen);
        add(btnCadastro);
        add(btnReconhecer);
        
        setupFrame();

        if (faceDetector.empty()) {
            showError("Erro ao carregar o classificador de rosto!");
        }
    }

    private void setupFrame() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void startCamera() {
        Mat image = new Mat();
        MatOfRect faces = new MatOfRect();
        while (isCameraActive && capture.read(image)) {
            faceDetector.detectMultiScale(image, faces);
            for (Rect rect : faces.toArray()) {
                Imgproc.rectangle(image, rect.tl(), rect.br(), new Scalar(0, 255, 0), 2);
            }
            cameraScreen.setIcon(new ImageIcon(matToBytes(image)));
        }
    }


    private byte[] matToBytes(Mat image) {
        MatOfByte buf = new MatOfByte();
        Imgcodecs.imencode(".jpg", image, buf);
        return buf.toArray();
    }

    private void loginAdm() {
    	String password = JOptionPane.showInputDialog(this, "Digite a senha de Administrador: (Padrão: admin)");
    	if(password.equals("admin")) {
    		cadastrarFace();
    	}
    	else {
    		JOptionPane.showMessageDialog(this, "Senha Inválida. Cadastro cancelado.");
    	}
    }
    private void cadastrarFace() {
        clicked = true;  // Indica que a imagem deve ser capturada
        // Captura a imagem, verifica se há rostos detectados e salva na pasta de cadastro
        Mat image = new Mat();
        if (capture.read(image)) {
            MatOfRect faces = new MatOfRect();
            faceDetector.detectMultiScale(image, faces);

            // Verifica se algum rosto foi detectado
            if (faces.toArray().length > 0) {
                String name = JOptionPane.showInputDialog(this, "Digite o nome para cadastro:");
                if (name != null && !name.trim().isEmpty()) {
                    // Salva a imagem na pasta de cadastro
                    Imgcodecs.imwrite("cadastro/" + name + ".jpg", image);
                    JOptionPane.showMessageDialog(this, "Face cadastrada com sucesso!");
                } else {
                    JOptionPane.showMessageDialog(this, "Nome inválido. Cadastro cancelado.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Nenhuma face detectada. Tente novamente.");
            }
        }
        clicked = false;  // Reseta a variável após cadastro
    }
 // Método para reconhecer uma face comparando com imagens cadastradas
    private int reconhecerFace() {
        clicked = true;  // Indica que a imagem deve ser capturada para reconhecimento

        Mat image = new Mat();
        if (capture.read(image)) {
            MatOfRect faces = new MatOfRect();
            faceDetector.detectMultiScale(image, faces);

            // Verifica se algum rosto foi detectado
            if (faces.toArray().length > 0) {
                String[] imagesList = new java.io.File("cadastro/").list();
                if (imagesList == null || imagesList.length == 0) {
                    JOptionPane.showMessageDialog(this, "Nenhuma face cadastrada para reconhecimento. Contate o administrador do programa.");
                    return 0;
                }

                boolean matchFound = false;
                for (String fileName : imagesList) {
                    Mat registeredFace = Imgcodecs.imread("cadastro/" + fileName);
                    
                    // Compara a face capturada com as cadastradas (simples comparação de tamanhos)
                    if (image.size().equals(registeredFace.size())) {
                        JOptionPane.showMessageDialog(this, "Face reconhecida: " + fileName.replace(".jpg", ""));
                        matchFound = true;
                        return 1;
                    }
                }

                if (!matchFound) {
                    JOptionPane.showMessageDialog(this, "Face não reconhecida!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Nenhuma face detectada. Tente novamente.");
            }
        }
        clicked = false;  // Reseta a variável após reconhecimento
        return 0;
    }

    
    private void abrirSegundaTela() {
    	isCameraActive = false;
        if (capture.isOpened()) {
            capture.release();
        }
        JButton btn1 = new JButton("Adicionar Fatia");
        JLabel text = new JLabel("0");  
        JLabel qntFatias = new JLabel("Conte quantas fatias de pizza você comeu no rodízio.");  
        int[] fatias = {0};  

        btn1.addActionListener(e -> {
            fatias[0]++;  
            text.setText(String.valueOf(fatias[0]));  
        });

        btn1.setBounds(670, 300, 200, 40);
        text.setBounds(765, 400, 200, 40);
        qntFatias.setBounds(620, 250, 300, 40);

        add(btn1);
        add(text);
        add(qntFatias);

        repaint();  // Atualiza a interface para exibir os novos componentes
    }


    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        EventQueue.invokeLater(() -> {
            Camera camera = new Camera();
            new Thread(camera::startCamera).start();
        });
    }
}
