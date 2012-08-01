/*
 *  This file is part of the Origin-World game client.
 *  Copyright (C) 2012 Arkadiy Fattakhov <ark@ark.su>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, version 3 of the License.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package a1_updater;
import java.awt.*;
import java.beans.*;
import java.io.*;
import java.net.NoRouteToHostException;
import javax.swing.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Properties;

public class ProgressBarMain extends JPanel
        implements PropertyChangeListener {

    private JProgressBar progressBar;
    private JTextArea taskOutput;
    private Task task;
    private boolean is_error;

    final String resource_remote_host = "upd.origin-world.com";
    final String updater_wrk = "updater.wrk";
    
    private class JobVal {
        private String msg;
        private int val;

        public JobVal(String msg, int val) {
            this.msg = msg;
            this.val = val;
        } 
        
        public String getMsg() {
            return msg;
        }
        
        public int getVal() {
            return val;
        }
    }

    class Task extends SwingWorker<Void, Void> {
        int current;
        /*
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() {
            File f = new File(updater_wrk);
            f.delete();
            
            ParseFiles();
            
            if (!is_error) {
                try {
                    Thread.sleep(1200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    BufferedWriter out = new BufferedWriter(new FileWriter("updater.wrk"));
                    out.write("done");
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    SendProgressMsg("fail create upd file", -1);
                }
            }
            return null;
        }

        public void ParseFiles() {
            try {
                is_error = false;
                SendProgressMsg("upd server: "+resource_remote_host, -1);
                URL url = new URL(new URI("http", resource_remote_host, "/res/files.txt","").toASCIIString());
                URLConnection c;
                c = url.openConnection();
                c.addRequestProperty("User-Agent", "a1_updater");
                SendProgressMsg("try get update info...", 0);
                InputStream in = c.getInputStream();

                Properties p = new Properties();
                p.load(in);
                SendProgressMsg("info is obtained", -1);
                progressBar.setMaximum(p.size());

                current = 0;
                for (Enumeration<Object> e = p.keys(); e.hasMoreElements();) {
                    String name = (String) e.nextElement();
                    current++;

                    // если это дллка и мы не под виндой. пропускаем.
                    if (
                            (name.contains("windows") && name.contains(".dll") && !isWindows()) ||
                            (name.contains("linux") && name.contains(".so") && !isLinux()) ||
                            (name.contains("freebsd") && name.contains(".so") && !isFreeBSD())
                    ){
                        Log.info("file: "+name+" not for this OS, skip");
                        SendProgressMsg("file: "+name+" not for this OS, skip", -1);
                    } else {
                        String fname = name;
                        if (name.contains("/"))
                            fname = name.substring(name.lastIndexOf("/")+1);
                        File f = new File(fname);
                        String fm = "file: "+name;
                        if (!f.exists()) {
                            SendProgressMsg(fm + " does not exist", -1);
                            DownloadFile(name);
                        } else {
                            int len = Integer.parseInt(p.getProperty(name));
                            if (f.length() != len) {
                                Log.info("file: "+name+" size does not match. delete...");
                                SendProgressMsg(fm+" need update, delete...", -1);
                                if (f.delete())
                                    DownloadFile(name);
                                else {
                                    SendProgressMsg("cant delete file: "+name, -1);
                                    Log.info("ERROR: cant delete file: "+name);
                                    is_error = true;
                                }
                            } else {
                                Log.info("skip file: "+name);
                                SendProgressMsg(fm+" OK", -1);
                            }
                        }
                    }
                    SendProgressMsg("", current);
                }

            } catch (NoRouteToHostException enr) {
                Log.info("ERROR: no route to host");
                SendProgressMsg("update server unavailable", 0);
            } catch (Exception e) {
                e.printStackTrace();
                Log.info("ERROR: failed update client");
                SendProgressMsg("error, see console log", 0);
            }
        }

        public void DownloadFile(String name) {
            try {
                SendProgressMsg("download file: " + name + "...", -1);
                Log.info("download file: "+name+"...");
                // грузим файл с сервера
                URL url = new URL(new URI("http", resource_remote_host, "/res/"+name,"").toASCIIString());
                URLConnection c;
                c = url.openConnection();
                c.addRequestProperty("User-Agent", "a1_updater");
                InputStream in = c.getInputStream();

                String fname = name;
                if (name.contains("/"))
                    fname = name.substring(name.lastIndexOf("/")+1);
                File f=new File(fname);
                OutputStream out=new FileOutputStream(f);

                byte buf[]=new byte[4096];
                int len;
                while((len=in.read(buf))>0)
                    out.write(buf,0,len);
                out.close();
                in.close();

                Log.info(name+" downloaded!");
                SendProgressMsg("downloaded!", current);
            } catch (Exception e) {
                e.printStackTrace();
                Log.info("ERROR: failed download file");
                is_error = true;
            }
        }
        

        private void SendProgressMsg(String msg, int percent) {
            JobVal v = new JobVal(msg, percent);
            firePropertyChange("prg", null, v);
        }

        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
//            Toolkit.getDefaultToolkit().beep();
            System.exit(0);
        }
    }

    public ProgressBarMain() {
        super(new BorderLayout());

        //Create the demo's UI.

        progressBar = new JProgressBar(0, 10);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        taskOutput = new JTextArea(5, 20);
        taskOutput.setMargin(new Insets(5,5,5,5));
        taskOutput.setEditable(false);
        taskOutput.setCursor(null); //inherit the panel's cursor
        //see bug 4851758

        JPanel panel = new JPanel();
        progressBar.setPreferredSize(new Dimension(330,30));
        panel.add(progressBar);

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(taskOutput), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        task = new Task();
        task.addPropertyChangeListener(this);
        task.execute();
    }

    /**
     * Invoked when task's progress property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("prg".equals(evt.getPropertyName())) {
            JobVal v = (JobVal) evt.getNewValue();
            if (v.getVal() >= 0)
                progressBar.setValue(v.getVal());
            if (v.getMsg().length() > 0)
                taskOutput.append(v.getMsg() + "\n");
        }
    }


    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        JFrame.setDefaultLookAndFeelDecorated(false);

        //Create and set up the window.
        JFrame frame = new JFrame("Origin updater ver 1.0");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(450, 300));

        //Create and set up the content pane.
        JComponent newContentPane = new ProgressBarMain();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        Log.info("a1 updater. rev: "+Const.svn_revision+"  buid: "+Const.build_date);
        Log.info("os: "+System.getProperty("os.name").toLowerCase());
        Log.info("os ver: "+System.getProperty("os.version").toLowerCase());
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    public static boolean isWindows() {

        String os = System.getProperty("os.name").toLowerCase();
        // windows
        return (os.contains("win"));
    }

    public static boolean isMac() {

        String os = System.getProperty("os.name").toLowerCase();
        // Mac
        return (os.contains("mac"));
    }

    public static boolean isLinux() {

        String os = System.getProperty("os.name").toLowerCase();
        // linux
        return (os.contains("nux"));
    }
    public static boolean isFreeBSD() {

        String os = System.getProperty("os.name").toLowerCase();
        return (os.contains("freebsd"));
    }
}