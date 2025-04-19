// Evaluador.java - Proyecto completo mejorado visualmente y funcional
// Autor: William Alejandro Ruiz Zelada

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class Evaluador extends JFrame {
    private JTextField campoExpresion, campoVariable, valorVariable;
    private JTextArea pilaOperandosDisplay, pilaOperadoresDisplay, erroresDisplay, historialDisplay, consolaDisplay;
    private HashMap<String, Integer> variables;
    private Stack<Integer> pilaOperandos;
    private Stack<Character> pilaOperadores;
    private Map<Character, Integer> usoOperadores;
    private JComboBox<String> comboVariables;
    private JMenuBar barraMenu;
    private JMenu menuArchivo, menuAyuda;
    private JMenuItem itemGuardar, itemCargar, itemSalir, itemAcercaDe;
    private DefaultListModel<String> historial;

    public Evaluador() {
        setTitle("Evaluador de Expresiones Avanzado");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        inicializarEstructuras();
        inicializarComponentes();
        inicializarEventos();
    }

    private void inicializarEstructuras() {
        variables = new HashMap<>();
        pilaOperandos = new Stack<>();
        pilaOperadores = new Stack<>();
        usoOperadores = new HashMap<>();
        historial = new DefaultListModel<>();
    }

    private void inicializarComponentes() {
        Font fuenteGeneral = new Font("Segoe UI", Font.PLAIN, 16);
        Font fuenteBoton = new Font("Segoe UI", Font.BOLD, 18);
        Color bgColor = new Color(240, 240, 255);
        Color buttonColor = new Color(66, 135, 245);

        barraMenu = new JMenuBar();
        menuArchivo = new JMenu("Archivo");
        menuAyuda = new JMenu("Ayuda");

        itemGuardar = new JMenuItem("Guardar Historial");
        itemCargar = new JMenuItem("Cargar Variables");
        itemSalir = new JMenuItem("Salir");
        itemAcercaDe = new JMenuItem("Acerca de");

        menuArchivo.add(itemGuardar);
        menuArchivo.add(itemCargar);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);
        menuAyuda.add(itemAcercaDe);

        barraMenu.add(menuArchivo);
        barraMenu.add(menuAyuda);
        setJMenuBar(barraMenu);

        JPanel panelSuperior = new JPanel();
        panelSuperior.setLayout(new BoxLayout(panelSuperior, BoxLayout.Y_AXIS));
        panelSuperior.setBackground(bgColor);
        panelSuperior.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel panelVar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        panelVar.setBackground(bgColor);
        comboVariables = new JComboBox<>();
        comboVariables.setFont(fuenteGeneral);
        valorVariable = new JTextField(10);
        valorVariable.setFont(fuenteGeneral);
        valorVariable.setEditable(false);
        panelVar.add(new JLabel("Variables:")).setFont(fuenteGeneral);
        panelVar.add(comboVariables);
        panelVar.add(new JLabel("Valor:")).setFont(fuenteGeneral);
        panelVar.add(valorVariable);

        JPanel panelExp = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        panelExp.setBackground(bgColor);
        campoExpresion = new JTextField(45);
        campoExpresion.setFont(fuenteGeneral);
        JButton btnEvaluar = new JButton("Evaluar");
        btnEvaluar.setFont(fuenteGeneral);
        panelExp.add(campoExpresion);
        panelExp.add(btnEvaluar);

        JPanel panelCrear = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        panelCrear.setBackground(bgColor);
        campoVariable = new JTextField(12);
        campoVariable.setFont(fuenteGeneral);
        JButton btnCrear = new JButton("Crear Variable");
        btnCrear.setFont(fuenteGeneral);
        panelCrear.add(new JLabel("Variable:")).setFont(fuenteGeneral);
        panelCrear.add(campoVariable);
        panelCrear.add(btnCrear);

        panelSuperior.add(panelVar);
        panelSuperior.add(panelExp);
        panelSuperior.add(panelCrear);

        JPanel panelBotones = new JPanel(new GridLayout(5, 5, 10, 10));
        panelBotones.setBackground(bgColor);
        panelBotones.setBorder(new EmptyBorder(10, 20, 10, 20));

        String[] botones = {"(", ")", "*", "/", "+", "-", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "@", "=", "CE", "CL", "Hist", "Del", "^", "%"};
        for (String s : botones) {
            JButton b = new JButton(s);
            b.setFont(fuenteBoton);
            b.setBackground(buttonColor);
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
            b.addActionListener(e -> manejarOperacion(s));
            panelBotones.add(b);
        }

        JPanel panelInferior = new JPanel(new GridLayout(2, 3, 10, 10));
        panelInferior.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelInferior.setPreferredSize(new Dimension(0, 250));

        pilaOperandosDisplay = crearTextArea(fuenteGeneral);
        pilaOperadoresDisplay = crearTextArea(fuenteGeneral);
        erroresDisplay = crearTextArea(fuenteGeneral);
        historialDisplay = crearTextArea(fuenteGeneral);
        consolaDisplay = crearTextArea(fuenteGeneral);
        consolaDisplay.setText("Consola interactiva\n> ");
        consolaDisplay.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    ejecutarComandoConsola();
                }
            }
        });

        panelInferior.add(crearScrollPane("Pila de Operandos", pilaOperandosDisplay));
        panelInferior.add(crearScrollPane("Estadísticas de Operadores", pilaOperadoresDisplay));
        panelInferior.add(crearScrollPane("Errores", erroresDisplay));
        panelInferior.add(crearScrollPane("Historial", historialDisplay));
        panelInferior.add(crearScrollPane("Consola", consolaDisplay));

        add(panelSuperior, BorderLayout.NORTH);
        add(panelBotones, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);

        btnEvaluar.addActionListener(e -> evaluarExpresion());
        btnCrear.addActionListener(e -> crearVariable());
        comboVariables.addActionListener(e -> {
            String sel = (String) comboVariables.getSelectedItem();
            if (variables.containsKey(sel)) valorVariable.setText(variables.get(sel).toString());
        });
    }

    private JTextArea crearTextArea(Font font) {
        JTextArea area = new JTextArea();
        area.setFont(font);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return area;
    }

    private JScrollPane crearScrollPane(String titulo, JTextArea area) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), titulo));
        return scroll;
    }

    private void inicializarEventos() {
        itemSalir.addActionListener(e -> salir());
        itemGuardar.addActionListener(e -> guardarHistorial());
        itemCargar.addActionListener(e -> cargarVariables());
        itemAcercaDe.addActionListener(e -> mostrarInfo("Evaluador de Expresiones Pro\nAutor: William Alejandro\nVersión: 2.0"));

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                salir();
            }
        });
    }

    private void salir() {
        int op = JOptionPane.showConfirmDialog(this, "¿Deseas salir?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (op == JOptionPane.YES_OPTION) dispose();
    }

    private void guardarHistorial() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("historial.txt"))) {
            writer.write(historialDisplay.getText());
            mostrarInfo("Historial guardado en historial.txt");
        } catch (IOException e) {
            mostrarError("No se pudo guardar historial");
        }
    }

    private void cargarVariables() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre archivo de variables (.txt)");
        if (nombre == null) return;
        try (Scanner sc = new Scanner(new File(nombre))) {
            while (sc.hasNext()) {
                String var = sc.next();
                int val = sc.nextInt();
                variables.put(var, val);
                comboVariables.addItem(var);
            }
            mostrarInfo("Variables cargadas desde archivo");
        } catch (Exception e) {
            mostrarError("Error al cargar variables: " + e.getMessage());
        }
    }

    private void mostrarInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    private void mostrarError(String msg) {
        erroresDisplay.setText(msg);
    }

    private void crearVariable() {
        String var = campoVariable.getText();
        if (var.isEmpty()) {
            mostrarError("Nombre de variable vacío");
            return;
        }
        try {
            int val = Integer.parseInt(JOptionPane.showInputDialog(this, "Valor para " + var));
            variables.put(var, val);
            comboVariables.addItem(var);
            mostrarInfo("Variable " + var + " = " + val);
        } catch (NumberFormatException e) {
            mostrarError("Valor inválido");
        }
    }

    private void manejarOperacion(String op) {
        switch (op) {
            case "=" -> evaluarExpresion();
            case "@" -> desplegarPilas();
            case "CE" -> campoExpresion.setText("");
            case "CL" -> {
                erroresDisplay.setText("");
                pilaOperandosDisplay.setText("");
                pilaOperadoresDisplay.setText("");
            }
            case "Hist" -> historialDisplay.setText("");
            case "Del" -> {
                String texto = campoExpresion.getText();
                if (!texto.isEmpty()) campoExpresion.setText(texto.substring(0, texto.length() - 1));
            }
            default -> campoExpresion.setText(campoExpresion.getText() + op);
        }
    }

    private int precedencia(char c) {
        return switch (c) {
            case '+', '-' -> 1;
            case '*', '/', '%', '^' -> 2;
            default -> 0;
        };
    }

    private void procesarOperadores() {
        if (pilaOperandos.size() < 2) {
            mostrarError("Error: Operador sin suficientes operandos.");
            throw new RuntimeException("Operandos insuficientes");
        }
        char op = pilaOperadores.pop();
        int b = pilaOperandos.pop();
        int a = pilaOperandos.pop();
        pilaOperandos.push(aplicarOperador(a, b, op));
    }

    private int aplicarOperador(int a, int b, char op) {
        try {
            return switch (op) {
                case '+' -> a + b;
                case '-' -> a - b;
                case '*' -> a * b;
                case '/' -> {
                    if (b == 0) throw new ArithmeticException("División por cero detectada");
                    yield a / b;
                }
                case '%' -> {
                    if (b == 0) throw new ArithmeticException("Módulo por cero detectado");
                    yield a % b;
                }
                case '^' -> (int) Math.pow(a, b);
                default -> throw new IllegalArgumentException("Operador desconocido: " + op);
            };
        } catch (ArithmeticException e) {
            mostrarError("Error aritmético: " + e.getMessage());
            throw e;
        }
    }

    private void evaluarExpresion() {
        String expr = campoExpresion.getText();
        pilaOperandos.clear();
        pilaOperadores.clear();
        erroresDisplay.setText("");

        try {
            for (int i = 0; i < expr.length(); i++) {
                char c = expr.charAt(i);
                if (Character.isWhitespace(c)) continue;
                if (Character.isDigit(c)) {
                    StringBuilder sb = new StringBuilder();
                    while (i < expr.length() && Character.isDigit(expr.charAt(i))) sb.append(expr.charAt(i++));
                    i--;
                    pilaOperandos.push(Integer.parseInt(sb.toString()));
                } else if (Character.isLetter(c)) {
                    StringBuilder sb = new StringBuilder();
                    while (i < expr.length() && Character.isLetterOrDigit(expr.charAt(i))) sb.append(expr.charAt(i++));
                    i--;
                    String nombre = sb.toString();
                    if (variables.containsKey(nombre)) pilaOperandos.push(variables.get(nombre));
                    else {
                        mostrarError("Variable no encontrada: " + nombre);
                        return;
                    }
                } else if (c == '(') {
                    pilaOperadores.push(c);
                } else if (c == ')') {
                    while (!pilaOperadores.isEmpty() && pilaOperadores.peek() != '(') procesarOperadores();
                    if (!pilaOperadores.isEmpty()) pilaOperadores.pop();
                } else if ("+-*/%^".indexOf(c) >= 0) {
                    while (!pilaOperadores.isEmpty() && precedencia(pilaOperadores.peek()) >= precedencia(c)) procesarOperadores();
                    usoOperadores.put(c, usoOperadores.getOrDefault(c, 0) + 1);
                    pilaOperadores.push(c);
                } else {
                    mostrarError("Carácter no válido: " + c);
                    return;
                }
            }
            while (!pilaOperadores.isEmpty()) procesarOperadores();
            if (!pilaOperandos.isEmpty()) {
                int resultado = pilaOperandos.pop();
                historialDisplay.append(expr + " = " + resultado + "\n");
                mostrarInfo("Resultado: " + resultado);
            }
        } catch (ArithmeticException ae) {
            // Ya mostrado
        } catch (Exception e) {
            mostrarError("Error inesperado: " + e.getMessage());
        }
        desplegarPilas();
    }

    private void desplegarPilas() {
        pilaOperandosDisplay.setText("Pila: " + pilaOperandos);
        StringBuilder estadisticas = new StringBuilder("Uso de operadores:\n");
        for (char op : usoOperadores.keySet()) {
            estadisticas.append(op).append(" → ").append(usoOperadores.get(op)).append(" veces\n");
        }
        pilaOperadoresDisplay.setText(estadisticas.toString());
    }

    private void ejecutarComandoConsola() {
        String texto = consolaDisplay.getText();
        String[] lineas = texto.split("\n");
        String ultimaLinea = lineas[lineas.length - 1].replaceFirst("^>\\s*", "").trim();

        if (ultimaLinea.isEmpty()) return;

        if (ultimaLinea.equalsIgnoreCase("HELP")) {
            consolaDisplay.append("\n> Comandos disponibles:\n" +
                    "> SET x = 5      → Crea o actualiza la variable x con valor 5\n" +
                    "> EVAL x + 3     → Evalúa una expresión usando variables o números\n" +
                    "> HELP           → Muestra esta ayuda\n> ");
        } else if (ultimaLinea.startsWith("SET ")) {
            try {
                String[] partes = ultimaLinea.substring(4).split("=");
                String nombre = partes[0].trim();
                int valor = Integer.parseInt(partes[1].trim());
                variables.put(nombre, valor);
                if (((DefaultComboBoxModel<String>) comboVariables.getModel()).getIndexOf(nombre) == -1) {
                    comboVariables.addItem(nombre);
                }
                consolaDisplay.append("\n> Variable " + nombre + " = " + valor + "\n> ");
            } catch (Exception e) {
                                consolaDisplay.append("\n> Error al procesar SET. Formato: SET x = 5\n> ");
            }
        } else if (ultimaLinea.startsWith("EVAL ")) {
            try {
                String expr = ultimaLinea.substring(5).trim();
                campoExpresion.setText(expr);
                evaluarExpresion();
                consolaDisplay.append("\n> Evaluado: " + expr + "\n> ");
            } catch (Exception e) {
                consolaDisplay.append("\n> Error al evaluar expresión\n> ");
            }
        } else {
            consolaDisplay.append("\n> Comando no reconocido. Escribe HELP para ver los comandos disponibles\n> ");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Evaluador().setVisible(true));
    }
}

