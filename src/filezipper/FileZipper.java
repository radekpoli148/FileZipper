package filezipper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

public class FileZipper extends JFrame {

    public FileZipper()
    {
        this.setTitle("Zipper");
        this.setBounds(250, 300, 600, 250);
        this.setJMenuBar(pasekMenu);
        
        JMenu menuPlik = pasekMenu.add(new JMenu("Plik"));
        
        Action akcjaDodawania = new Akcja("Dodaj", "Dodaj nowy wpis do archiwum", "ctrl D", new ImageIcon("dodaj.png"));
        Action akcjaUsuwania = new Akcja("Usun", "Usun wpis z archiwum", "ctrl U", new ImageIcon("usun.png"));
        Action akcjaZipowania = new Akcja("Zip", "Zipuj", "ctrl Z");
        
        JMenuItem menuOtworz = menuPlik.add(akcjaDodawania);
        JMenuItem menuUsun = menuPlik.add(akcjaUsuwania);
        JMenuItem menuZip = menuPlik.add(akcjaZipowania);
        
        bDodaj = new JButton(akcjaDodawania);
        bUsun = new JButton(akcjaUsuwania);
        bZip = new JButton(akcjaZipowania);
        
        JScrollPane scroll = new JScrollPane(lista);
        
        lista.setBorder(BorderFactory.createEtchedBorder());
        
        GroupLayout layout = new GroupLayout(this.getContentPane());
        
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                .addComponent(scroll, 100, 150, Short.MAX_VALUE)
                .addContainerGap(0, Short.MAX_VALUE)
                .addGroup(
                layout.createParallelGroup()
                        .addComponent(bDodaj)
                        .addComponent(bUsun)
                        .addComponent(bZip)
                )
        );
        layout.setVerticalGroup(
                layout.createParallelGroup()
                .addComponent(scroll, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(
                layout.createSequentialGroup()
                        .addComponent(bDodaj)
                        .addComponent(bUsun)
                        .addGap(5, 40, Short.MAX_VALUE)
                        .addComponent(bZip)
                )
        );
        
        this.getContentPane().setLayout(layout);
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //this.pack();
    }
    
    private DefaultListModel modelListy = new DefaultListModel()
    {
        @Override
        public void addElement(Object obj)
        {
            lista.add(obj);
            super.addElement(((File)obj).getName());
        }
        
        @Override
        public Object get(int index) 
        {
            return lista.get(index);
        }
        
        @Override
        public Object remove(int index) 
        {
            lista.remove(index);
            return super.remove(index);
        }

        ArrayList lista = new ArrayList();
    };
    private JList lista = new JList(modelListy);
    private JButton bDodaj;
    private JButton bUsun;
    private JButton bZip;
    private JMenuBar pasekMenu = new JMenuBar();
    private JFileChooser wybieracz = new JFileChooser();
    
    public static void main(String[] args) 
    {
        new FileZipper().setVisible(true);
    }
    
    private class Akcja extends AbstractAction
    {
        public Akcja(String nazwa, String opis, String klawiatorowySkrot)
        {
            this.putValue(Action.NAME, nazwa);
            this.putValue(Action.SHORT_DESCRIPTION, opis);
            this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(klawiatorowySkrot));
        }
        public Akcja(String nazwa, String opis, String klawiatorowySkrot, Icon ikona)
        {
            this(nazwa, opis, klawiatorowySkrot);
            this.putValue(Action.SMALL_ICON, ikona);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getActionCommand().equals("Dodaj"))
                dodajWpisDoArchiwum();
            else if(e.getActionCommand().equals("Usun"))
                usuwanieWpisuZArchiwum();
            else if(e.getActionCommand().equals("Zip"))
                stworzArchiwumZip();
        }
        
        private void dodajWpisDoArchiwum()
        {
            wybieracz.setCurrentDirectory(new File(System.getProperty("user.dir")));
            wybieracz.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            wybieracz.setMultiSelectionEnabled(true);
            int tmp = wybieracz.showDialog(rootPane, "Dodaj do archiwum");
            
            if(tmp == JFileChooser.APPROVE_OPTION)
            {
                File[] sciezki = wybieracz.getSelectedFiles();
                
                for(int i = 0; i < sciezki.length; i++)
                    if(!czyWpisSiePowtarza(sciezki[i].getPath()))
                        modelListy.addElement(sciezki[i]);
            }
        }
        
        private boolean czyWpisSiePowtarza(String testowany)
        {
            for(int i = 0; i<modelListy.getSize(); i++)
                if( ((File)modelListy.get(i)).getPath().equals(testowany))
                    return true;
            
            return false;
        }
        
        private void usuwanieWpisuZArchiwum()
        {
            int tmp[] = lista.getSelectedIndices();
            
            for(int i = 0; i<tmp.length; i++)
                modelListy.remove(tmp[i]-i);
        }
        
        private void stworzArchiwumZip()
        {
            wybieracz.setCurrentDirectory(new File(System.getProperty("user.dir")));
            wybieracz.setSelectedFile(new File(System.getProperty("user.dir")+File.separator+"MojaNazwa.zip"));
            int tmp = wybieracz.showDialog(rootPane, "Kompresuj");
            
            if(tmp == JFileChooser.APPROVE_OPTION)
            {
                byte tmpData[] = new byte[buffor];
                try
                {
                    ZipOutputStream zOutS = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(wybieracz.getSelectedFile()),buffor));

                    for(int i = 0; i < modelListy.getSize(); i++)
                    {
                        if(!((File)modelListy.get(i)).isDirectory())
                            zipuj(zOutS, (File)modelListy.get(i), tmpData, ((File)modelListy.get(i)).getPath());
                        else
                        {
                            wypiszScierzki((File)modelListy.get(i));
                            
                            for(int j = 0; j < listaScierzek.size(); j++)
                                zipuj(zOutS, (File)listaScierzek.get(j), tmpData, ((File)modelListy.get(i)).getPath());
                            
                            listaScierzek.removeAll(listaScierzek);
                        }
                    }
                    zOutS.close();
                }
                catch(IOException e)
                {
                    System.out.println(e.getMessage());
                }
            }
        }
        private void zipuj(ZipOutputStream zOutS, File scierzkaPliku, byte[] tmpData, String scierzkaBazowa) throws IOException
        {
            BufferedInputStream inS = new BufferedInputStream(new FileInputStream(scierzkaPliku), buffor);

            zOutS.putNextEntry(new ZipEntry(scierzkaPliku.getPath().substring(scierzkaBazowa.lastIndexOf(File.separator)+1)));

            int counter;
            while((counter = inS.read(tmpData, 0, buffor))!= -1)
                zOutS.write(tmpData, 0, counter);

            zOutS.closeEntry();
            inS.close();
        }
        public static final int buffor = 1024;
        
        private void wypiszScierzki(File nazwaScierzki)
        {
            String[] nazwyPlikówIKatalogow = nazwaScierzki.list();
            System.out.println(nazwaScierzki.getPath());
            for(int i = 0; i < nazwyPlikówIKatalogow.length; i++)
            {
                File p = new File(nazwaScierzki.getPath(), nazwyPlikówIKatalogow[i]);
                if(p.isFile())
                    listaScierzek.add(p);

                if(p.isDirectory())
                    wypiszScierzki(new File(p.getPath()));
            }
        }
        ArrayList listaScierzek = new ArrayList();
        
    }
}
