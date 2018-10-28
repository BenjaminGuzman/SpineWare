package titlebar;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Healthynnovation
 */
public class TitleBar_Min_Max_Close extends TitleBar{
    public TitleBar_Min_Max_Close(boolean resizable, JFrame jf){
        super(resizable, jf);
        this.add(createButtons(), BorderLayout.EAST);
    }
    private JPanel createButtons(){
        JPanel buttons = new JPanel(new BorderLayout());
        buttons.add(new Min(jf), BorderLayout.WEST);
        buttons.add(new Max(jf), BorderLayout.CENTER);
        buttons.add(new Close(jf), BorderLayout.EAST);
        return buttons;
    }
    /*public TitleBar_Min_Max_Close(boolean r, JDialog jd){
        super(r, jd);
        JBTN_CLOSE = new JButton();
        JBTN_MIN = new JButton();
        JBTN_MAX = new JButton();
        try{
            JBTN_CLOSE.setIcon(new ImageIcon(this.getClass().getResource("close.png")));
        } catch(NullPointerException e){
            JOptionPane.showMessageDialog(null, "Falta: \"close.png\".\nPor favor no modifiques algún archivo", "Missing: \"close.png\"", JOptionPane.WARNING_MESSAGE);
            JBTN_CLOSE.setText("X");
        } catch (Exception e){
            JOptionPane.showMessageDialog(null, "Ocurrió un error al abrir \"close.png\"", "Error in \"close.png\"", 0);
            JBTN_CLOSE.setText("X");
        }
        try{
            JBTN_MIN.setIcon(new ImageIcon(this.getClass().getResource("minimize.png")));
        } catch (NullPointerException e){
            JOptionPane.showMessageDialog(null, "Falta: \"minimize.png\".\nPor favor no modifiques algún archivo", "Missing: \"minimize.png\"", JOptionPane.WARNING_MESSAGE);
            JBTN_MIN.setText("-");
        } catch (Exception e){
            JOptionPane.showMessageDialog(null, "Ocurrió un error al abrir \"minimize.png\"", "Error in \"minimize.png\"", 0);
            JBTN_MIN.setText("-");
        }
        GRID = new GridLayout(1, 2);
        BUTTONS = new JPanel();
        initComponents();
        listeners();
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = .25;
        this.add(BUTTONS, gbc);
    }
    public TitleBar_Min_Max_Close(boolean r, JFrame jf){
        super(r, jf);
        JBTN_CLOSE = new JButton();
        JBTN_MIN = new JButton();
        JBTN_MAX = new JButton();
        try{
            JBTN_CLOSE.setIcon(new ImageIcon(this.getClass().getResource("close.png")));
        } catch(NullPointerException e){
            JOptionPane.showMessageDialog(null, "Falta: \"close.png\".\nPor favor no modifiques algún archivo", "Missing: \"close.png\"", JOptionPane.WARNING_MESSAGE);
            JBTN_CLOSE.setText("X");
        } catch (Exception e){
            JOptionPane.showMessageDialog(null, "Ocurrió un error al abrir \"close.png\"", "Error in \"close.png\"", 0);
            JBTN_CLOSE.setText("X");
        }
        try{
            JBTN_MIN.setIcon(new ImageIcon(this.getClass().getResource("minimize.png")));
        } catch (NullPointerException e){
            JOptionPane.showMessageDialog(null, "Falta: \"minimize.png\".\nPor favor no modifiques algún archivo", "Missing: \"minimize.png\"", JOptionPane.WARNING_MESSAGE);
            JBTN_MIN.setText("-");
        } catch (Exception e){
            JOptionPane.showMessageDialog(null, "Ocurrió un error al abrir \"minimize.png\"", "Error in \"minimize.png\"", 0);
            JBTN_MIN.setText("-");
        }
        try{
            JBTN_MAX.setIcon(new ImageIcon(this.getClass().getResource("maximize.png")));
        } catch (NullPointerException e){
            JOptionPane.showMessageDialog(null, "Falta: \"minimize.png\".\nPor favor no modifiques algún archivo", "Missing: \"minimize.png\"", JOptionPane.WARNING_MESSAGE);
            JBTN_MAX.setText("0");
        } catch (Exception e){
            JOptionPane.showMessageDialog(null, "Ocurrió un error al abrir \"minimize.png\"", "Error in \"minimize.png\"", 0);
            JBTN_MAX.setText("0");
        }
        GRID = new GridLayout(1, 2);
        BUTTONS = new JPanel();
        initComponents();
        listeners();
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = .25;
        this.add(BUTTONS, gbc);
    }
    @Override
    protected void initComponents(){
        JBTN_MIN.setToolTipText("Minimizar");
        personalize(JBTN_MIN);
        
        JBTN_CLOSE.setToolTipText("Cerrar");
        personalize(JBTN_CLOSE);
        
        JBTN_MAX.setToolTipText("Maximizar");
        personalize(JBTN_MAX);
        
        BUTTONS.setLayout(GRID);
        BUTTONS.setBackground(LF.NATIVE);
        BUTTONS.add(JBTN_MIN);
        BUTTONS.add(JBTN_MAX);
        BUTTONS.add(JBTN_CLOSE);
    }
    @Override
    protected void personalize(JButton btn){
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBackground(LF.NATIVE);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 1));
    }
    @Override
    protected void listeners(){
        JBTN_CLOSE.addMouseListener(new HoverF(jf, JBTN_CLOSE, (byte)0));
        JBTN_MAX.addMouseListener(new HoverF(jf, JBTN_MAX, (byte)1));
        JBTN_MIN.addMouseListener(new HoverF(jf, JBTN_MIN, (byte)2));
        JBTN_MIN.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent evt){
                
            }
        });
        JBTN_CLOSE.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent evt){
                destroy();
            }
        });
        JBTN_MAX.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent evt){
                
            }
        });
    }*/
}
