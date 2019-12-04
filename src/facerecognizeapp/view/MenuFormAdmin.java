/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facerecognizeapp.view;



import java.io.IOException;
import java.util.ArrayList;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import javax.imageio.ImageIO;


import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import java.awt.Image;
import java.awt.image.BufferedImage;

import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;

import facerecognizeapp.CaptureCameraFrameUsingOpenCv2;
import facerecognizeapp.DrawImage;
import facerecognizeapp.Utilities;
import facerecognizeapp.sql.Control;
import facerecognizeapp.sql.FDA_Sinhvien;
import facerecognizeapp.sql.FDA_Lichsudiemdanh;
import facerecognizeapp.sql.FDA_Canbo;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.filechooser.FileNameExtensionFilter;
/**
 *
 * @author macbook
 */
public class MenuFormAdmin extends javax.swing.JFrame {
    
    public enum Privilege {
        ADMIN,
        USER
    }

    /**
     * Creates new form MenuForm
     */
    private static final int DEFAULT_AMOUNT_OF_FRAME_STORED = 3;
    private static final int DEFAULT_AMOUNT_OF_FRAME_COLLECT = 10;
    private static final String[] DEFAULT_FACE_ANGLES = new String[] {"font_face", "left_face", "right_face"};
    private static final int END_OF_FRAME = -1;
    private static final int END_OF_FRAME_LIST = -2;
    private static final String CURRENT_WORKING_DIR = System.getProperty("user.dir");
    private static final String FILE_PATH_SEPARATE = System.getProperty("file.separator");
    private static final String DEFAULT_IMAGE_PATH = CURRENT_WORKING_DIR + FILE_PATH_SEPARATE + "images" + FILE_PATH_SEPARATE + "canbo";
    private static final Object LOCK_VIDEOSTREAM_THREAD = new Object();
    private static final String SEVER_IP = "localhost";
    private static final int SEVER_PORT = 5555;
    
    private boolean stopVideoStream = false;
    private boolean blockAddMoreFrame;
    private boolean stopRecognize;
    private int labelIndex;
    
    private Socket socket;
    private Thread videoStreamThread;
    private Thread serverResponseHanlderThread;
    private Control dataRepository;
    private FDA_Canbo adminInfo;

    private List<FDA_Lichsudiemdanh> attendedStudentList;
    private List<FDA_Sinhvien> newStudentList;
    private List<Image> frameList;
    private BufferedReader reader;
    private BufferedWriter writer;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    private CaptureCameraFrameUsingOpenCv2 captureCameraFrameUsingOpenCv2;
    private final ServerResponseHandler serverResponseHandler;
    private final CollectDataForTrain collecDataForTrain;
    private final CollecDataForRecognize collecDataForRecognize;
   
    
//    
    
    public MenuFormAdmin() {
        setLookAndFeel();
        try {
            prepareBeforeExecution();
        } catch(SQLException ex) {
            ex.printStackTrace();
            System.out.println("Can't connect to DataBase");
            dataBaseConnectionErrorHandle();
        } catch(Exception ex) {
            ex.printStackTrace();
        } 
        initComponents();
        DrawImage drawFramePanel = (DrawImage)jPanel4;
        serverResponseHandler = new ServerResponseHandler(socket, drawFramePanel);
        collecDataForRecognize = new CollecDataForRecognize((DrawImage)jPanel4);
        collecDataForTrain = new CollectDataForTrain(socket, null, null, DEFAULT_AMOUNT_OF_FRAME_COLLECT);
    }
    
    public MenuFormAdmin(String adminId, Privilege privilege) {
        setLookAndFeel();
        try {
            prepareBeforeExecution();
        } catch(SQLException ex) {
            ex.printStackTrace();
            System.out.println("Can't connect to DataBase");
            dataBaseConnectionErrorHandle();
        } catch(Exception ex) {
            ex.printStackTrace();
        } 
        initComponents();
        adminInfo = dataRepository.getCanBo(adminId);
        showAdminInfo(adminInfo);
        DrawImage drawFramePanel = (DrawImage)jPanel4;
        serverResponseHandler = new ServerResponseHandler(socket, drawFramePanel);
        collecDataForRecognize = new CollecDataForRecognize((DrawImage)jPanel4);
        collecDataForTrain = new CollectDataForTrain(socket, null, null, DEFAULT_AMOUNT_OF_FRAME_COLLECT);
        if(privilege == Privilege.USER) {
            jTabbedPane3.remove(2);
        }
    }
    
    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MenuFormAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MenuFormAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MenuFormAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MenuFormAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }
    
    
    private Socket connectToServer(String host, int port) throws IOException {
        Socket sock;
        sock = new Socket(host, port);
        System.out.println(sock.getPort());
        System.out.println(sock.getLocalPort());
        System.out.println(sock.getInetAddress().toString()); 
        return sock;
    }
    
    private void openStreamFromServer(Socket socket) throws IOException {
        InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
        reader = new BufferedReader(streamReader);
        OutputStreamWriter streamWriter = new OutputStreamWriter(socket.getOutputStream());
        writer = new BufferedWriter(streamWriter);
    }
    
    private void prepareBeforeExecution() throws IOException, SQLException{
        try {
            socket = connectToServer(SEVER_IP, SEVER_PORT);
        } catch(ConnectException ex) {
            System.out.println("Can't connect to sever");
            ex.printStackTrace();
            socketConnectionErrorHandle();
        }
        openStreamFromServer(socket);
        frameList = new ArrayList<>(DEFAULT_AMOUNT_OF_FRAME_STORED);
        dataRepository = new Control();
        newStudentList = new ArrayList<>(1);
        captureCameraFrameUsingOpenCv2 = new CaptureCameraFrameUsingOpenCv2();
        
        
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFrame1 = new javax.swing.JFrame();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField11 = new javax.swing.JTextField();
        jTextField12 = new javax.swing.JTextField();
        jTextField13 = new javax.swing.JTextField();
        jPanel10 = new DrawImage();
        jButton2 = new javax.swing.JButton();
        jPanel11 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new DrawImage();
        jPanel5 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        ngaysinh = new javax.swing.JTextField();
        gioitinh = new javax.swing.JTextField();
        nganh = new javax.swing.JTextField();
        khoa = new javax.swing.JTextField();
        khoas = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        mssv = new javax.swing.JTextField();
        name_sv = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanel16 = new javax.swing.JPanel();
        jLabel26 = new javax.swing.JLabel();
        trangthaidiemdanh = new javax.swing.JTextField();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jid = new javax.swing.JTextField();
        jname = new javax.swing.JTextField();
        jngaysinh = new javax.swing.JTextField();
        jgioitinh = new javax.swing.JTextField();
        jnganh = new javax.swing.JTextField();
        jkhoas = new javax.swing.JTextField();
        jkhoa = new javax.swing.JTextField();
        jButton5 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jPanel17 = new javax.swing.JPanel();
        jPanel14 = new DrawImage();
        jPanel15 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        dateChooserCombo = new datechooser.beans.DateChooserCombo();
        jTextField4 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel27 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame1Layout.setVerticalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                formComponentHidden(evt);
            }
        });

        jTabbedPane3.setToolTipText("");
        jTabbedPane3.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane3StateChanged(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("THÔNG TIN CÁ NHÂN");

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));

        jLabel11.setText("Họ và tên");

        jLabel13.setText("Ngày Sinh");

        jLabel14.setText("Giới tính");

        jLabel15.setText("Mã số cán bộ");

        jLabel16.setText("Khoa");

        jLabel17.setText("Chức vụ");

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jTextField11.setEditable(false);

        jTextField12.setEditable(false);

        jTextField13.setEditable(false);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField1)
                    .addComponent(jTextField2)
                    .addComponent(jTextField3)
                    .addComponent(jTextField11, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                    .addComponent(jTextField12)
                    .addComponent(jTextField13))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextField3, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextField11, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextField12, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextField13, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
                .addContainerGap(132, Short.MAX_VALUE))
        );

        jPanel10.setBackground(new java.awt.Color(255, 255, 153));
        jPanel10.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel10.setPreferredSize(new java.awt.Dimension(114, 116));

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 172, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 205, Short.MAX_VALUE)
        );

        jButton2.setText("Đổi Ảnh");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 825, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(67, 67, 67)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(63, 63, 63)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton2)))
                .addContainerGap(128, Short.MAX_VALUE))
        );

        jPanel11.setBackground(new java.awt.Color(255, 255, 255));
        jPanel11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel12.setFont(new java.awt.Font("Lucida Grande", 1, 18)); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("MENU");

        jButton3.setText("ĐĂNG XUẤT");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton6.setText("LƯU THÔNG TIN");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane3.addTab("Quản Lý", jPanel1);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 640, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 480, Short.MAX_VALUE)
        );

        jPanel5.setBackground(new java.awt.Color(204, 204, 255));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Thông tin người điểm danh");

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel4.setText("Họ và tên:");

        jLabel5.setText("Ngày sinh:");

        jLabel6.setText("Giới tính:");

        jLabel8.setText("Ngành đào tạo:");

        jLabel9.setText("Khóa:");

        jLabel10.setText("Khoa:");

        ngaysinh.setEditable(false);

        gioitinh.setEditable(false);

        nganh.setEditable(false);

        khoa.setEditable(false);

        khoas.setEditable(false);

        jLabel7.setText("Mã sinh viên:");

        mssv.setEditable(false);

        name_sv.setEditable(false);
        name_sv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                name_svActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                        .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ngaysinh)
                    .addComponent(gioitinh)
                    .addComponent(nganh)
                    .addComponent(khoa)
                    .addComponent(khoas, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                    .addComponent(mssv, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                    .addComponent(name_sv, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mssv, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(name_sv, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ngaysinh, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(gioitinh, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(nganh, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(khoa, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(khoas, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE))
                .addContainerGap(39, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBackground(new java.awt.Color(51, 204, 255));
        jPanel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("TRẠNG THÁI DIỂM DANH");

        jPanel16.setBackground(new java.awt.Color(255, 255, 255));

        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel26.setText(">>>");

        trangthaidiemdanh.setEditable(false);

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(trangthaidiemdanh))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(trangthaidiemdanh))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButton7.setText("BẮT ĐẦU ĐIỂM DANH");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.setText("DỪNG CAMERA");
        jButton8.setEnabled(false);
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jButton8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(50, Short.MAX_VALUE))
        );

        jTabbedPane3.addTab("Điểm Danh", jPanel2);

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));

        jPanel12.setBackground(new java.awt.Color(204, 204, 255));

        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("Thông tin người điểm danh");

        jPanel13.setBackground(new java.awt.Color(255, 255, 255));

        jLabel19.setText("Họ Và Tên:");

        jLabel20.setText("Ngày Sinh (yyyy-mm-dd):");

        jLabel21.setText("Giới Tính:");

        jLabel22.setText("Mã Sinh Viên:");

        jLabel23.setText("Ngành Đào Tạo:");

        jLabel24.setText("Khoa:");

        jLabel25.setText("Khóa:");

        jid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jidActionPerformed(evt);
            }
        });

        jname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jnameActionPerformed(evt);
            }
        });

        jButton5.setText("GỬI DỮ LIỆU");
        jButton5.setEnabled(false);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton9.setText("XÓA TẤT CẢ");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jLabel29.setText("Mặt Thẳng:");

        jLabel30.setText("Mặt Nghiêng Trái:");

        jLabel31.setText("Mặt Nghiêng Phải:");

        jLabel32.setText("0/10");

        jLabel33.setText("0/10");

        jLabel34.setText("0/10");

        jLabel28.setForeground(new java.awt.Color(255, 0, 0));
        jLabel28.setText("*");

        jLabel35.setForeground(new java.awt.Color(255, 0, 0));
        jLabel35.setText("*");

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton9))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jLabel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel23, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel24, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel21, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel20, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel13Layout.createSequentialGroup()
                                    .addComponent(jLabel35)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGap(15, 15, 15)))
                            .addGroup(jPanel13Layout.createSequentialGroup()
                                .addComponent(jLabel28)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel29)
                            .addComponent(jLabel31)
                            .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(52, 52, 52)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jid)
                            .addComponent(jname)
                            .addComponent(jngaysinh)
                            .addComponent(jgioitinh)
                            .addComponent(jnganh)
                            .addComponent(jkhoas)
                            .addComponent(jkhoa, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                            .addGroup(jPanel13Layout.createSequentialGroup()
                                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel32)
                                    .addComponent(jLabel33)
                                    .addComponent(jLabel34))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jid, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel28))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jname, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel35))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jngaysinh, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jgioitinh, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jnganh, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jkhoas, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addComponent(jLabel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jkhoa, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(52, 52, 52)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(jLabel32))
                .addGap(18, 18, 18)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel30)
                    .addComponent(jLabel33))
                .addGap(18, 18, 18)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel34))
                .addGap(52, 52, 52)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButton4.setText("CHỤP ẢNH");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jPanel17.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jPanel14.setBackground(new java.awt.Color(204, 255, 255));
        jPanel14.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 636, Short.MAX_VALUE)
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 474, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel17, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jTabbedPane3.addTab("Thêm Sinh Viên", jPanel8);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Mã Sinh Viên", "Tên Sinh Viên", "Giới Tính", "Ngày Điểm Danh", "Thời Gian Điểm Danh"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane2.setViewportView(jTable1);

        dateChooserCombo.setCalendarPreferredSize(new java.awt.Dimension(350, 280));
        dateChooserCombo.setLocale(new java.util.Locale("vi", "VN", ""));
        dateChooserCombo.addCommitListener(new datechooser.events.CommitListener() {
            public void onCommit(datechooser.events.CommitEvent evt) {
                dateChooserComboOnCommit(evt);
            }
        });

        jTextField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField4ActionPerformed(evt);
            }
        });

        jButton1.setText("Tìm");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel27.setText("Nhập Mã Sinh Viên:");

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1115, Short.MAX_VALUE)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dateChooserCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(167, 167, 167)
                .addComponent(jLabel27)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dateChooserCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton1)
                        .addComponent(jLabel27)))
                .addGap(59, 59, 59)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 555, Short.MAX_VALUE))
        );

        jTabbedPane3.addTab("Lịch Sử Điểm Danh", jPanel15);

        jMenu1.setText("File");

        jMenuItem1.setText("Setting");
        jMenu1.add(jMenuItem1);

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setText("Exit");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Help");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane3)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane3, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentHidden
        // TODO add your handling code here:
    }//GEN-LAST:event_formComponentHidden

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jTabbedPane3StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane3StateChanged
        // TODO add your handling code here:
        if(jTabbedPane3.getSelectedIndex() != 1) {
            captureCameraFrameUsingOpenCv2.releaseCamera();
            jButton7.setEnabled(true);
            jButton8.setEnabled(false);
            stopVideoStream = true;
            stopRecognize = true;
            clearTextField();
            try {
                //            serverResponseHanlderThread.interrupt();
                //            serverResponseHanlderThread = null;
                //            thread.interrupt();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        if(jTabbedPane3.getSelectedIndex() != 2) {
            newStudentList.clear();
            clearNewStudentInfoFill();
            labelIndex = 0;
        }

        if(jTabbedPane3.getSelectedIndex() == 3) {
            Date currDate = dateChooserCombo.getSelectedDate().getTime();
            getAttendanceHistoryOfCurrentDate(currDate);
        }

    }//GEN-LAST:event_jTabbedPane3StateChanged

    private void dateChooserComboOnCommit(datechooser.events.CommitEvent evt) {//GEN-FIRST:event_dateChooserComboOnCommit
        // TODO add your handling code here:
        Date currDate = dateChooserCombo.getSelectedDate().getTime();
        jTextField4.setText("");
        clearAttendanceTable();
        getAttendanceHistoryOfCurrentDate(currDate);
        
    }//GEN-LAST:event_dateChooserComboOnCommit

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        if(!isStudentInfoFullFill()) {
            return;
        }
//        String imageForTrainFolderPath = CURRENT_WORKING_DIR + FILE_PATH_SEPARATE + "ImageForTrain";
//        File f = new File(imageForTrainFolderPath);
//        if(!f.exists()) {
//            f.mkdir();
//        }
        JLabel[] labels = new JLabel[] {
            jLabel32,
            jLabel33,
            jLabel34
        };
        
        String studentId = null;
        try {
            FDA_Sinhvien student = getNewStudentInfo();
            studentId = student.getMssv();
        } catch(ParseException ex) {
            ex.printStackTrace();
            return;
        } catch(Exception ex) {
            ex.printStackTrace();
            return;
        }
        
        DrawImage drawFramePanel = (DrawImage)jPanel14;
        collecDataForTrain.setDrawImage(drawFramePanel);
        collecDataForTrain.setLabel(labels[labelIndex]);
        collecDataForTrain.setFaceAngle(DEFAULT_FACE_ANGLES[labelIndex]);
        collecDataForTrain.setFaceId(studentId);
        Thread t = new Thread(collecDataForTrain);
        t.start();
        jButton4.setEnabled(false);
        labelIndex += 1;

    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // TODO add your handling code here:
        
        try {
            if(deleteTrainImageOnServer(jid.getText())){
                showMessage("NOTIFICATION", "Xóa thành công");
            }else {
                showMessage("ERROR", "Xóa thất bại");
            }
        }catch(IOException ex) {
            showMessage("ERROR", "ERROR OCCURED!");
            ex.printStackTrace();
        }
        newStudentList.clear();
        clearNewStudentInfoFill();
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        if(checkStudentInfoFullFill()) {
            showMessage("ERROR", "Thông Tin Sinh Viên Chưa Đầy Đủ");
            return;
        }
          
        try {
            newStudentList.add(getNewStudentInfo());
            if(insertStudentToDatabase(newStudentList.get(0))) {
                showMessage("NOTIFICATION", "Lưu dữ liệu thành công");
            } else {
                showMessage("NOTIFICATION", "Lưu dữ liệu thất bại");
            }
//            sendImageToServerToTrain(reader, writer);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        newStudentList.clear();
        clearNewStudentInfoFill();
        jButton5.setEnabled(false);
        jButton4.setEnabled(true);
        labelIndex = 0;
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jnameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jnameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jnameActionPerformed

    private void jidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jidActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jidActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
        captureCameraFrameUsingOpenCv2.releaseCamera();
        stopVideoStream = true;
        stopRecognize = true;
//        serverResponseHanlderThread = null;
        clearTextField();
        jButton7.setEnabled(true);
        jButton8.setEnabled(false);
        jTabbedPane3.setEnabled(true);
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
        jTabbedPane3.setEnabled(false);
        jButton7.setEnabled(false);
        jButton8.setEnabled(true);
        stopVideoStream = false;
        stopRecognize = false;
        frameList.clear();
        videoStreamThread = new Thread(collecDataForRecognize);
        videoStreamThread.start();
        serverResponseHanlderThread = new Thread(serverResponseHandler);
        serverResponseHanlderThread.start();
    }//GEN-LAST:event_jButton7ActionPerformed

    private void name_svActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_name_svActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_name_svActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        //        int opt=JOptionPane.showConfirmDialog(null, "Bạn muốn đăng xuất ?", "LOGOUT", JOptionPane.YES_NO_OPTION);
        //        if( opt==0){
            //            new LoginForm().setVisible(true);
            //            this.dispose();
            //        }

        boolean isLogout = showMessage("YesNo", "Bạn muốn đăng xuất ?");
        if(isLogout) {
            try {
                closeAllStream();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            this.dispose();
            new LoginForm().setVisible(true);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        Date currDate = dateChooserCombo.getSelectedDate().getTime();
        String studentCode = jTextField4.getText();
        if(!Utilities.isStudentCodeValid(studentCode)) {
            showMessage("ERROR", "Mã sinh viên không hợp lệ");
            return;
        }
        if(!getAttendanceHistoryOfCurrentDateAndStudentCode(studentCode, currDate)) {
            showMessage("ERROR", "Sinh viên chưa điểm danh");
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
        // TODO add your handling code here:
        Date currDate = dateChooserCombo.getSelectedDate().getTime();
        String studentCode = jTextField4.getText();
        if(!Utilities.isStudentCodeValid(studentCode)) {
            showMessage("ERROR", "Mã sinh viên không hợp lệ");
            return;
        }
        if(!getAttendanceHistoryOfCurrentDateAndStudentCode(studentCode, currDate)) {
            showMessage("ERROR", "Sinh viên chưa điểm danh");
        }
    }//GEN-LAST:event_jTextField4ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "JPG & PNG Images", "jpg", "png");
        fc.setFileFilter(filter);
        fc.showOpenDialog(this);
        File f = fc.getSelectedFile();
        if(f == null) {
            return;
        }
        if(!f.exists()) {
            return;
        }
        DrawImage drawImage = (DrawImage)jPanel10;
        ImageIcon imageIcon = new ImageIcon(f.getAbsolutePath());
        drawImage.drawImage(imageIcon.getImage());
//        if(adminInfo.getAvatarPath() != null) {
//            f = new File(adminInfo.getAvatarPath());
//            if(f.exists()) {
//                f.delete();
//            }
//        }
        adminInfo.setAvatarPath(f.getAbsolutePath());  
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
        FDA_Canbo admin = new FDA_Canbo();
        admin.setTencb(jTextField1.getText());
        try {
            admin.setNgaysinh(simpleDateFormat.parse(jTextField2.getText()));
        } catch (ParseException ex) {
            ex.printStackTrace();
            Logger.getLogger(MenuFormAdmin.class.getName()).log(Level.SEVERE, null, ex);
        }
        admin.setGioitinh(jTextField3.getText());
        admin.setMscb(adminInfo.getMscb());
        admin.setKhoa(adminInfo.getKhoa());
        admin.setBomon(adminInfo.getBomon());
        
        File f = new File(adminInfo.getAvatarPath());
        
        File newImagePath = new File(DEFAULT_IMAGE_PATH);
        if(!newImagePath.exists()) {
            newImagePath.mkdirs();
        }
        
        newImagePath = new File(DEFAULT_IMAGE_PATH + FILE_PATH_SEPARATE + f.getName());
        try {
            Files.copy(f.toPath(), newImagePath.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(MenuFormAdmin.class.getName()).log(Level.SEVERE, null, ex);
            adminInfo = dataRepository.getCanBo(adminInfo.getMscb());
            showAdminInfo(adminInfo);
        }
        
        admin.setAvatarPath("images" + FILE_PATH_SEPARATE + "canbo" + FILE_PATH_SEPARATE + f.getName());
        if(dataRepository.updateCanBo(admin)) {
            showMessage("NOTIFICATION", "Cập nhật thành công");
        } else {
            
        }
        adminInfo = dataRepository.getCanBo(adminInfo.getMscb());
        showAdminInfo(adminInfo);
    }//GEN-LAST:event_jButton6ActionPerformed
    
    
    
    class CollecDataForRecognize implements Runnable {
        private final DrawImage drawImage;
        
        public CollecDataForRecognize(DrawImage drawImage) {
            this.drawImage = drawImage;
        }
        
        public void run() {
            Mat frameInMatType;
            Image frameInImageType;
            if(captureCameraFrameUsingOpenCv2.openCamera(0)){
                System.out.println("Camera OK?");
            }
            else{
                System.out.println("Camera Error");
            }
            while(true) {
                synchronized (LOCK_VIDEOSTREAM_THREAD) {
//                        System.out.println("Video stream not block");
                    if(stopVideoStream) {
                        stopVideoStream = false;
                        break;
                    }
                    frameInMatType = captureCameraFrameUsingOpenCv2.captureFrame();
                    if(frameInMatType.width() <= 0) {
                        continue;
                    }
                    frameInImageType = HighGui.toBufferedImage(frameInMatType);
                    if(!blockAddMoreFrame) {
                        if(frameList.size() >= DEFAULT_AMOUNT_OF_FRAME_STORED) {
                            frameList.remove(0);
                        }
                        frameList.add(frameInImageType);
                    }
                    drawImage.drawImage(frameInImageType);
                    try {
                        Thread.sleep(50);
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            captureCameraFrameUsingOpenCv2.releaseCamera();
        }
    }
    
    public boolean saveImageToFile(byte[] imageData, String imagePath) throws IOException {
        File f;
        BufferedOutputStream bos;
        f = new File(imagePath);
        bos = new BufferedOutputStream(new FileOutputStream(f));    
        bos.write(imageData);
        bos.flush();
        bos.close();
        return true;
    }
    
    public boolean saveImageToFile(Image image, String imagePath) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write((BufferedImage)image, "jpg", byteArrayOutputStream);
        byte[] imageData = byteArrayOutputStream.toByteArray();
        return saveImageToFile(imageData, imagePath);
    }
    
    public boolean isStudentInfoFullFill() {
        JTextField[] jTextFields = new JTextField[]{
            jid,
            jname,
            jngaysinh,
            jgioitinh,
            jnganh,
            jkhoas,
            jkhoa};
        String studentId = jTextFields[0].getText();
//        for(JTextField jTextField : jTextFields) {
//            if(jTextField.getText().length() == 0) {
//                showMessage("ERROR", "Thông Tin Sinh Viên Chưa Điền Đầy Đủ");
//                return false;
//            }
//        }
        
        if((studentId != null) && (studentId.length() > 0)) {
            return true;
        }
        showMessage("ERROR", "Thông Tin Sinh Viên Chưa Điền Đầy Đủ");
        return false;
        
    }
    
    public void sendSignal(String signal, BufferedWriter writer) throws IOException {
        writer.write(signal);
        writer.flush();
    }
    
    public void closeAllStream() throws IOException, SQLException{
        System.out.println("Close all stream");
        reader.close();
        writer.close();
        socket.close();
        dataRepository.closeConnection();
        
    }
    
    
    class CollectDataForTrain implements Runnable {
        private int amountsOfImageNeedCollect;
        private JLabel label;
        private DrawImage drawImage;
       
        private Socket socket;
        private BufferedReader reader;
        private BufferedWriter writer;
        private String faceAngle;
        private String faceId;
        
        public CollectDataForTrain(Socket socket, DrawImage drawImage, JLabel label, int amountsOfImageNeedCollect) {
            this.socket = socket;
            this.drawImage = drawImage;
            this.amountsOfImageNeedCollect = amountsOfImageNeedCollect;
            this.label = label;
            try {
                InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
                reader = new BufferedReader(streamReader);
                OutputStreamWriter streamWriter = new OutputStreamWriter(socket.getOutputStream());
                writer = new BufferedWriter(streamWriter);
                System.out.println("networking established");
            }catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Can't connect to sever");
            }
        }
        
        public void setAmountsOfImageNeedCollect(int amountsOfImageNeedCollect) {
            this.amountsOfImageNeedCollect = amountsOfImageNeedCollect;
        }

        public void setLabel(JLabel label) {
            this.label = label;
        }

        public void setDrawImage(DrawImage drawImage) {
            this.drawImage = drawImage;
        }

        public void setFaceAngle(String faceAngle) {
            this.faceAngle = faceAngle;
        }

        public void setFaceId(String faceId) {
            this.faceId = faceId;
        }
           
        public void run() {
            Mat frame;
            Image image;
            Image imageContainFace = null;
            boolean delay = true;
            List<Image> frameList = new ArrayList<>(1);
            
            jTabbedPane3.setEnabled(false);
            jButton9.setEnabled(false);
            
            try {
                sendSignal("DETECT\n", writer);
                if(!reader.readLine().equalsIgnoreCase("OK")) {
                    return;
                }
                sendSignal(faceId + "\n" + faceAngle + "\n", writer);
                if(!reader.readLine().equalsIgnoreCase("OK")) {
                    return;
                }
                captureCameraFrameUsingOpenCv2.openCamera(0);
                //wait camera ready for capture frame
                Thread.sleep(1000);
                for(int i = 1; i <= amountsOfImageNeedCollect; i++) {
                    i -= 1;
                    image = captureCameraFrameUsingOpenCv2.captureImage();
                    frameList.add(0, image);
                    sendSignal("FRAME\n", writer);
                    sendImageListToServer(frameList, writer);
                    imageContainFace = waitImageResponseFromServer(reader);
                    if(imageContainFace != null) {
                        label.setText(i + "/" + amountsOfImageNeedCollect);
                        image = imageContainFace;
                    } else {
                        i -= 1;
                    }
                    drawImage.drawImage(image);
                }
                sendSignal("EXIT\n", writer);
            }catch(IOException ex) {
                ex.printStackTrace();
                socketConnectionErrorHandle();
            }catch(Exception ex) {
                ex.printStackTrace();
            } finally{
                captureCameraFrameUsingOpenCv2.releaseCamera();
                jButton4.setEnabled(true);
                jTabbedPane3.setEnabled(true);
                jButton9.setEnabled(true);
                if(labelIndex == DEFAULT_FACE_ANGLES.length){
                    jButton5.setEnabled(true);
                    jButton4.setEnabled(false);
                }
            }
        }
    }
    
    
    public boolean deleteTrainImageOnServer(String faceId) throws IOException {
        String signal;
        writer.write("DELETE\n");
        writer.flush();
        signal = reader.readLine();
        if(!signal.equalsIgnoreCase("OK")) {
            return false;
        }
        writer.write(faceId + "\n");
        writer.flush();
        signal = reader.readLine();
        System.out.println(signal);
        return signal.equalsIgnoreCase("success");
    }

    public Image waitImageResponseFromServer(BufferedReader reader) throws IOException {
        String signal = "";
        Image resultImage = null;
        
        signal = reader.readLine();
        
        if(signal.equalsIgnoreCase("FAILURE")) {
            return null;
        }
        if(signal.equalsIgnoreCase("IMAGE")) {
            signal = reader.readLine();
            byte[] imageData = Base64.getDecoder().decode(signal);  
            resultImage = ImageIO.read(new ByteArrayInputStream(imageData));
        }
        return resultImage;
    }
    
    public void sendImageToServer(Image image, BufferedWriter writer) throws IOException {
        BufferedImage bufferedImage = (BufferedImage)image;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpg", b);
        byte[] imgData = b.toByteArray();
        System.out.println(imgData.length);
        String encodedfile = new String(Base64.getEncoder().encode(imgData), "UTF-8");
        writer.write(encodedfile);
        writer.flush();
        socket.getOutputStream().write(END_OF_FRAME);
    }
    
    public void sendImageListToServer(List<Image> imageList, BufferedWriter writer) throws IOException {
        for(Image image : imageList) {
            sendImageToServer(image, writer);
        }
        socket.getOutputStream().write(END_OF_FRAME_LIST);
    }
    
    
    public class ServerResponseHandler implements Runnable {
        private Socket socket;
        private BufferedReader reader;
        private BufferedWriter writer;
        private DrawImage drawFramePanel;

        public ServerResponseHandler(Socket socket, DrawImage drawFramePanel) {
            this.drawFramePanel = (DrawImage)drawFramePanel;
            this.socket = socket;
            try {
                InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
                reader = new BufferedReader(streamReader);
                OutputStreamWriter streamWriter = new OutputStreamWriter(socket.getOutputStream());
                writer = new BufferedWriter(streamWriter);
                System.out.println("networking established");
            }catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Can't connect to sever");
            }
        }

//        public void sendFrameToServer(Mat frame) throws IOException {
//            BufferedImage bufferedImage;
//            byte[] inputData;
//            String encodedfile;
//            bufferedImage = (BufferedImage)HighGui.toBufferedImage(frame);
//            ByteArrayOutputStream b = new ByteArrayOutputStream();
//            ImageIO.write(bufferedImage, "jpg", b);
//            inputData = b.toByteArray();
////            System.out.println(inputData.length);
//            encodedfile = new String(Base64.getEncoder().encode(inputData), "UTF-8");
//            writer.write(encodedfile);
//            writer.flush();
//            socket.getOutputStream().write(END_OF_FRAME);
//        }

//        public void sendListOfFrameToServer(List<Mat> frameList) throws IOException {
//            for(Mat frame : frameList) {
//                sendFrameToServer(frame);
//            }
//            socket.getOutputStream().write(END_OF_FRAME_LIST);
//        }
      
//        public void closeAllStream() throws IOException{
//            System.out.println("Close all stream");
//            reader.close();
//            writer.close();
//        }
        
        public byte[] handleResponseContainBase64EncodedImage() throws IOException, InterruptedException {
            String encodedFile = reader.readLine();
            System.out.println("Image received: " + encodedFile.length());
            if(encodedFile.length() < 0) {
                return null;
            }
            return Base64.getDecoder().decode(encodedFile);
        }
        
        public void handleSuccessSignal() throws IOException, InterruptedException, ParseException {
            String signal;
            BufferedImage bufferedImage = null;
            String studentID = "";
            byte[] imageData;
            while((signal = reader.readLine()) != null) {
                System.out.println(signal);
                if(signal.equalsIgnoreCase("image")) {
                    imageData = handleResponseContainBase64EncodedImage();  
                    bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData));
                } else if(signal.equalsIgnoreCase("info")) {
                    studentID = reader.readLine();
                    if(stopRecognize) {
                        return;
                    }
                    synchronized (LOCK_VIDEOSTREAM_THREAD) {
                        //waiting video stream thread locked
                        Thread.sleep(100);
                        System.out.println("video stream blocked");
                        drawFramePanel.drawImage(bufferedImage);
                        showStudentInfo(studentID);
                        writeHistory();
                        //after 5 second. Begin recognize again
                        Thread.sleep(5000);
                        clearTextField();
                        frameList.clear();
                    }
                    break;
                } 
            }
            
        }
        

        public void run() {
            String signal;
            System.out.println("start");
            try {
                while(true) {
                    if(stopRecognize) {
                        return;
                    }
                    writer.write("RECOGNIZE\n");
                    writer.flush();
                    while((signal = reader.readLine()) != null) {
                        System.out.println("Receive signal: " + signal);
                        if(signal.equalsIgnoreCase("ok")) {
                            while(frameList.size() < DEFAULT_AMOUNT_OF_FRAME_STORED) {
                                if(stopRecognize) {
                                    System.out.println("Stop listen server response");
                                    frameList.clear();
                                    sendImageListToServer(frameList, writer);
                                    return;
                                }
                                Thread.sleep(100);
                            }
                            System.out.println(frameList.size());
                            if(frameList.size() == DEFAULT_AMOUNT_OF_FRAME_STORED) {
                                blockAddMoreFrame = true;
                                Thread.sleep(100);
                                sendImageListToServer(frameList, writer);
                                frameList.clear();
                                blockAddMoreFrame = false;
                            }
                            System.out.println("Send Frame");
                            System.out.println("End");
                            continue;
                        }
                        if(signal.equalsIgnoreCase("success")) {
                            handleSuccessSignal();
                        }
                        break;
                    }
                }
            } catch(SocketException ex) {
                ex.printStackTrace();
                socketConnectionErrorHandle();
                
            } catch(Exception ex) {
                ex.printStackTrace();
                
            }
        }   
    }
    
    
    private boolean insertStudentToDatabase(FDA_Sinhvien student) {
        if(student == null) {
            throw new IllegalArgumentException("ERROR: Student is null!!");
        }
        if(dataRepository.getSinhVien(student.getMssv()) != null) {
            return true;
        }
        return dataRepository.themSinhvien(student);
    }
    
    private void showStudentInfo(String sinhVienId){
        FDA_Sinhvien s = new FDA_Sinhvien();
        s = dataRepository.getSinhVien(sinhVienId);
        if(s == null) {
            return;
        }
        name_sv.setText(s.getTensv());
        ngaysinh.setText(s.getNgaysinhsv().toString());
        gioitinh.setText(s.getGioitinh());
        mssv.setText(s.getMssv());
        nganh.setText(s.getNganh());
        khoa.setText(s.getKhoa());
        khoas.setText(s.getKhoas());
        trangthaidiemdanh.setText("DA DIEM DANH");
        
    }
    
    private void showAdminInfo(FDA_Canbo admin){
        FDA_Canbo cb = null;
        //String image_url=c.getimage(i);
//        cb = sinhVienRepository.getCanBo(i);
        cb = admin;
        jTextField1.setText(cb.getTencb());
        jTextField2.setText(cb.getNgaysinh().toString());
        jTextField3.setText(cb.getGioitinh());
        jTextField11.setText(cb.getMscb());
        jTextField12.setText(cb.getKhoa());
        jTextField13.setText(cb.getBomon());
        if(cb.getAvatarPath() != null){
            DrawImage drawImage = (DrawImage)jPanel10;
            ImageIcon imageIcon = new ImageIcon(cb.getAvatarPath());
            drawImage.drawImage(imageIcon.getImage());
        }
        //draw_Image(image_url);
    }
    
    
    private FDA_Sinhvien getNewStudentInfo() throws ParseException {
        if(!checkStudentInfoFullFill()) {
            return null;
        }
        String dob = jngaysinh.getText();
        if(dob.length() == 0) {
            dob = "0001-01-01";
        }
        
        JTextField[] jTextFields = new JTextField[]{
            jgioitinh,
            jnganh,
            jkhoas,
            jkhoa
        };
        
        for(JTextField textField : jTextFields) {
            if(textField.getText().length() == 0) {
                textField.setText("Unknow");
            }
        }
        
        return new FDA_Sinhvien(
            jid.getText(),
            jname.getText(),
            simpleDateFormat.parse(dob),
            jgioitinh.getText(),
            jnganh.getText(),
            jkhoas.getText(),
            jkhoa.getText()
        );
        
//        return new FDA_Sinhvien(jid.getText());
    }
    
    
    private boolean checkInputStudentIdIsExists(String studentId) {
        return true;
    }
    
    
    public void updateAttendanceTable(List<FDA_Lichsudiemdanh> attendedStudentList) {
        clearAttendanceTable();
        DefaultTableModel model=(DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);
        Object[] row = new Object[5];
        for(int i=0; i< attendedStudentList.size(); i++ ){
            row[0] = attendedStudentList.get(i).getMssv();
            row[1] = attendedStudentList.get(i).getTensv();
            row[2] = attendedStudentList.get(i).getGioitinh();
            row[3] = attendedStudentList.get(i).getNgaydiemdanh();
            row[4] = attendedStudentList.get(i).getThoiGianDiemDanh();
            model.addRow(row);
        }
    }
    
    public void getAttendanceHistoryOfCurrentDate(Date currentDate) {
        attendedStudentList = dataRepository.doclichsu(currentDate);
        if(attendedStudentList.isEmpty()) {
            return;
        }
        System.out.print(attendedStudentList.size());
        updateAttendanceTable(attendedStudentList);
    }
    
    public boolean getAttendanceHistoryOfCurrentDateAndStudentCode(String studentCode, Date currentDate) {
        attendedStudentList = dataRepository.doclichsutheomssv(studentCode, currentDate);
        if(attendedStudentList.isEmpty()) {
            return false;
        }
        System.out.print(attendedStudentList.size());
        updateAttendanceTable(attendedStudentList);
        return true;
    }
    
    public void writeHistory() throws ParseException {
        FDA_Lichsudiemdanh lichsudiemdanh = new FDA_Lichsudiemdanh();
        lichsudiemdanh.setMssv(mssv.getText());
        lichsudiemdanh.setNgaydiemdanh(new Date());
        dataRepository.ghilichsu(lichsudiemdanh);
    }
    
    public void clearTextField() {
        JTextField[] textFields = new JTextField[] {
            name_sv,
            ngaysinh,
            gioitinh,
            mssv,
            nganh,
            khoa,
            khoas,
            trangthaidiemdanh,};
        
        for(JTextField textField : textFields) {
            textField.setText("");
        }
        ((DrawImage)jPanel4).clear();
        jPanel2.repaint();
    }
    
    private boolean checkStudentInfoFullFill() {
//        JTextField[] jTextFields = new JTextField[]{
//            jid,
//            jname,
//            jngaysinh,
//            jgioitinh,
//            jnganh,
//            jkhoas,
//            jkhoa};
        
        JTextField[] jTextFields = new JTextField[]{
            jid,
            jname};
        
        for(JTextField jTextField : jTextFields) {
            if(jTextField.getText().length() == 0) {
                return false;
            }
        }
        return true;
    }
    
    private void clearNewStudentInfoFill() {
        JTextField[] jTextFields = new JTextField[]{
            jid,
            jname,
            jngaysinh,
            jgioitinh,
            jnganh,
            jkhoas,
            jkhoa};
        JLabel[] labels = new JLabel[] {
            jLabel32,
            jLabel33,
            jLabel34
        };
        for(JTextField jTextField : jTextFields) {
            jTextField.setText("");
        }
        
        for(JLabel jLabel : labels) {
            jLabel.setText("0/" + DEFAULT_AMOUNT_OF_FRAME_COLLECT);
        }
        ((DrawImage)jPanel14).clear();
        jPanel17.repaint();
    }
    
    private void clearAttendanceTable() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);
    }
    
    private void socketConnectionErrorHandle() {
        while(true) {  
            if(showMessage("YesNo", "Kết nối tới Server bị lỗi! Kết nối lại!")) {
                try {
                    socket = connectToServer(SEVER_IP, SEVER_PORT);
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                this.dispose();
                System.exit(0);
            }
        }

        
    }
    
    private void dataBaseConnectionErrorHandle() {
        while(true) {
            if(showMessage("YesNo", "Kết nối tới Database bị lỗi! Kết nối lại!")) {
                try {
                    dataRepository = new Control();
                    break;
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                this.dispose();
                System.exit(0);
            }
        }
    }
    
    
    
    private boolean showMessage(String messageType, String title) {
        if(messageType.equals("ERROR")) {
            if(title == null) {
                JOptionPane.showMessageDialog(this, "Lỗi nghiêm trọng !! Vui lòng khởi động lại chương trình", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } else JOptionPane.showMessageDialog(this, title, "Lỗi", JOptionPane.ERROR_MESSAGE);
            return true;
            
        }
        if(messageType.equals("NOTIFICATION")) {
            if(title != null) {
                JOptionPane.showMessageDialog(this, title);
            } 
            return true;
        } 
        if(messageType.equals("YesNo")) {
            if(title != null) {
                int userChoice = JOptionPane.showConfirmDialog(this, title, "Lựa chọn", JOptionPane.YES_NO_OPTION);
//                System.out.println(userChoice);
                return userChoice == 0;
            }
        }
        return true;
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MenuFormAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MenuFormAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MenuFormAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MenuFormAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new MenuFormAdmin().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private datechooser.beans.DateChooserCombo dateChooserCombo;
    private javax.swing.JTextField gioitinh;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JFrame jFrame1;
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
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField11;
    private javax.swing.JTextField jTextField12;
    private javax.swing.JTextField jTextField13;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jgioitinh;
    private javax.swing.JTextField jid;
    private javax.swing.JTextField jkhoa;
    private javax.swing.JTextField jkhoas;
    private javax.swing.JTextField jname;
    private javax.swing.JTextField jnganh;
    private javax.swing.JTextField jngaysinh;
    private javax.swing.JTextField khoa;
    private javax.swing.JTextField khoas;
    private javax.swing.JTextField mssv;
    private javax.swing.JTextField name_sv;
    private javax.swing.JTextField nganh;
    private javax.swing.JTextField ngaysinh;
    private javax.swing.JTextField trangthaidiemdanh;
    // End of variables declaration//GEN-END:variables
}
