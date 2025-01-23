package com.opencvcamera;

import java.awt.*;
import javax.swing.*;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

public class Camera extends JFrame {
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
        btnReconhecer.addActionListener(e -> reconhecerFace());
        
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
        while (capture.read(image)) {
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
    	String password = JOptionPane.showInputDialog(this, "Digite a senha de Administrador:");
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
    private void reconhecerFace() {
        clicked = true;  // Indica que a imagem deve ser capturada para reconhecimento

        Mat image = new Mat();
        if (capture.read(image)) {
            MatOfRect faces = new MatOfRect();
            faceDetector.detectMultiScale(image, faces);

            // Verifica se algum rosto foi detectado
            if (faces.toArray().length > 0) {
                String[] imagesList = new java.io.File("cadastro/").list();
                if (imagesList == null || imagesList.length == 0) {
                    JOptionPane.showMessageDialog(this, "Nenhuma face cadastrada para reconhecimento.");
                    return;
                }

                boolean matchFound = false;
                for (String fileName : imagesList) {
                    Mat registeredFace = Imgcodecs.imread("cadastro/" + fileName);
                    
                    // Compara a face capturada com as cadastradas (simples comparação de tamanhos)
                    if (image.size().equals(registeredFace.size())) {
                        JOptionPane.showMessageDialog(this, "Face reconhecida: " + fileName.replace(".jpg", ""));
                        matchFound = true;
                        break;
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
