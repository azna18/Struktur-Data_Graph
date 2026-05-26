import java.util.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import javax.swing.*;

public class Graph {

    private Map<String, java.util.List<String>> adjList;
    private Set<String> vertices;
    private boolean isDirected;

    public Graph(boolean isDirected) {
        adjList = new LinkedHashMap<>();
        vertices = new LinkedHashSet<>();
        this.isDirected = isDirected;
    }

    public boolean isDirected() { return isDirected; }
    public Set<String> getVertices() { return vertices; }
    public Map<String, java.util.List<String>> getAdjList() { return adjList; }

    // 1. Tambah Vertex
    public void tambahVertex(String v) {
        v = v.toUpperCase();
        if (vertices.contains(v)) { System.out.println("Vertex " + v + " sudah ada!"); return; }
        vertices.add(v);
        adjList.put(v, new ArrayList<>());
        System.out.println("Vertex " + v + " berhasil ditambahkan.");
    }

    // 2. Hapus Vertex
    public void hapusVertex(String v) {
        v = v.toUpperCase();
        if (!vertices.contains(v)) { System.out.println("Vertex " + v + " tidak ditemukan!"); return; }
        vertices.remove(v);
        adjList.remove(v);
        for (String u : vertices) adjList.get(u).remove(v);
        System.out.println("Vertex " + v + " berhasil dihapus.");
    }

    // 3. Tambah Edge
    public void tambahEdge(String u, String v) {
        u = u.toUpperCase(); v = v.toUpperCase();
        if (!vertices.contains(u) || !vertices.contains(v)) { System.out.println("Salah satu atau kedua vertex tidak ditemukan!"); return; }
        if (adjList.get(u).contains(v)) { System.out.println("Edge " + u + (isDirected?" -> ":" - ") + v + " sudah ada!"); return; }
        adjList.get(u).add(v);
        if (!isDirected) adjList.get(v).add(u);
        System.out.println("Edge " + u + (isDirected?" -> ":" - ") + v + " berhasil ditambahkan.");
    }

    // 4. Hapus Edge
    public void hapusEdge(String u, String v) {
        u = u.toUpperCase(); v = v.toUpperCase();
        if (!vertices.contains(u) || !vertices.contains(v)) { System.out.println("Salah satu atau kedua vertex tidak ditemukan!"); return; }
        boolean removed = adjList.get(u).remove(v);
        if (!isDirected) adjList.get(v).remove(u);
        if (removed) System.out.println("Edge " + u + (isDirected?" -> ":" - ") + v + " berhasil dihapus.");
        else System.out.println("Edge " + u + (isDirected?" -> ":" - ") + v + " tidak ditemukan!");
    }

    // 5. Tampilkan Graph (teks di terminal)
    public void tampilkanGraph() {
        if (vertices.isEmpty()) { System.out.println("Graph kosong!"); return; }
        java.util.List<String> vList = new ArrayList<>(vertices);
        Collections.sort(vList);
        System.out.println("\n=== ADJACENCY LIST ===");
        for (String v : vList) {
            java.util.List<String> nb = new ArrayList<>(adjList.get(v));
            Collections.sort(nb);
            System.out.print("Vertex " + v + " -> ");
            System.out.println(nb.isEmpty() ? "(tidak ada tetangga)" : nb);
        }
        System.out.println("\n=== ADJACENCY MATRIX ===");
        System.out.print("    ");
        for (String v : vList) System.out.printf("%4s", v);
        System.out.println();
        for (String u : vList) {
            System.out.printf("%4s", u);
            for (String v : vList) System.out.printf("%4d", adjList.get(u).contains(v) ? 1 : 0);
            System.out.println();
        }
    }

    // 6. DFS
    public java.util.List<String> dfs(String start) {
        start = start.toUpperCase();
        if (!vertices.contains(start)) return null;
        java.util.List<String> result = new ArrayList<>();
        dfsHelper(start, new LinkedHashSet<>(), result);
        return result;
    }

    private void dfsHelper(String v, Set<String> visited, java.util.List<String> result) {
        visited.add(v); result.add(v);
        java.util.List<String> nb = new ArrayList<>(adjList.get(v));
        Collections.sort(nb);
        for (String n : nb) if (!visited.contains(n)) dfsHelper(n, visited, result);
    }

    // 7. BFS
    public java.util.List<String> bfs(String start) {
        start = start.toUpperCase();
        if (!vertices.contains(start)) return null;
        java.util.List<String> result = new ArrayList<>();
        Set<String> visited = new LinkedHashSet<>();
        Queue<String> queue = new LinkedList<>();
        visited.add(start); queue.add(start);
        while (!queue.isEmpty()) {
            String v = queue.poll(); result.add(v);
            java.util.List<String> nb = new ArrayList<>(adjList.get(v));
            Collections.sort(nb);
            for (String n : nb) if (!visited.contains(n)) { visited.add(n); queue.add(n); }
        }
        return result;
    }

    // ========== GUI PANEL ==========
    static class GraphPanel extends JPanel {
        private Graph graph;
        private Map<String, Point> pos = new LinkedHashMap<>();
        private java.util.List<String> traversal = new ArrayList<>();
        private int step = -1;
        private String label = "";

        static final int R = 26;
        static final Color C_BG   = new Color(30, 35, 45);
        static final Color C_NODE = new Color(100, 175, 220);
        static final Color C_VIS  = new Color(80, 200, 120);
        static final Color C_CUR  = new Color(255, 200, 60);
        static final Color C_EDGE = new Color(180, 200, 220);

        public GraphPanel(Graph g) {
            this.graph = g;
            setBackground(C_BG);
            setPreferredSize(new Dimension(600, 450));
            layout();
        }

        public void layout() {
            pos.clear();
            java.util.List<String> vList = new ArrayList<>(graph.getVertices());
            int n = vList.size(); if (n == 0) return;
            int cx = 300, cy = 225, rad = 160;
            if (n == 1) { pos.put(vList.get(0), new Point(cx, cy)); return; }
            for (int i = 0; i < n; i++) {
                double a = 2 * Math.PI * i / n - Math.PI / 2;
                pos.put(vList.get(i), new Point(cx + (int)(rad * Math.cos(a)), cy + (int)(rad * Math.sin(a))));
            }
        }

        public void setTraversal(java.util.List<String> t, String lbl) {
            traversal = new ArrayList<>(t); label = lbl; step = -1; repaint();
        }

        public void nextStep() {
            if (step < traversal.size() - 1) { step++; repaint(); }
        }

        public void resetStep() { step = -1; repaint(); }
        public int getStep() { return step; }
        public int getTotalSteps() { return traversal.size(); }
        public String getCurrent() { return step >= 0 ? traversal.get(step) : ""; }
        public java.util.List<String> getTraversal() { return new ArrayList<>(traversal); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // judul
            String title = (graph.isDirected() ? "Directed" : "Undirected") + " Graph"
                + (label.isEmpty() ? "" : "  —  " + label);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.setColor(new Color(210, 225, 245));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(title, (getWidth() - fm.stringWidth(title)) / 2, 28);

            if (graph.getVertices().isEmpty()) {
                g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
                g2.setColor(new Color(120, 140, 160));
                String msg = "Graph kosong.";
                g2.drawString(msg, (getWidth() - g2.getFontMetrics().stringWidth(msg)) / 2, getHeight() / 2);
                return;
            }

            Set<String> visited = new HashSet<>(traversal.subList(0, Math.max(0, step + 1)));

            // edge
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(C_EDGE);
            Set<String> drawn = new HashSet<>();
            for (String u : graph.getVertices()) {
                Point pu = pos.get(u); if (pu == null) continue;
                for (String v : graph.getAdjList().get(u)) {
                    Point pv = pos.get(v); if (pv == null) continue;
                    String key = graph.isDirected() ? u+"->"+v : (u.compareTo(v)<0?u+"-"+v:v+"-"+u);
                    if (drawn.contains(key)) continue; drawn.add(key);
                    if (graph.isDirected()) drawArrow(g2, pu, pv);
                    else g2.drawLine(pu.x, pu.y, pv.x, pv.y);
                }
            }

            // node
            for (String v : graph.getVertices()) {
                Point p = pos.get(v); if (p == null) continue;
                Color nc = v.equals(getCurrent()) ? C_CUR : visited.contains(v) ? C_VIS : C_NODE;
                g2.setColor(nc);
                g2.fillOval(p.x-R, p.y-R, R*2, R*2);
                g2.setColor(nc.darker());
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(p.x-R, p.y-R, R*2, R*2);

                // nomor langkah
                int idx = traversal.indexOf(v);
                if (idx >= 0 && idx <= step) {
                    g2.setColor(new Color(20, 20, 20, 200));
                    g2.fillOval(p.x+R-12, p.y-R-5, 17, 17);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                    String num = String.valueOf(idx+1);
                    FontMetrics nfm = g2.getFontMetrics();
                    g2.drawString(num, p.x+R-12+(17-nfm.stringWidth(num))/2, p.y-R-5+12);
                }

                // huruf
                g2.setFont(new Font("SansSerif", Font.BOLD, 15));
                g2.setColor(new Color(20, 30, 40));
                FontMetrics lm = g2.getFontMetrics();
                g2.drawString(v, p.x - lm.stringWidth(v)/2, p.y + lm.getAscent()/2 - 1);
            }

            // info langkah traversal
            if (!traversal.isEmpty() && step >= 0) {
                String info = "Langkah " + (step+1) + "/" + traversal.size() + "  |  Urutan: " + traversal.subList(0, step+1);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
                g2.setColor(new Color(180, 210, 180));
                g2.drawString(info, 10, getHeight() - 10);
            }
        }

        private void drawArrow(Graphics2D g2, Point from, Point to) {
            double ang = Math.atan2(to.y-from.y, to.x-from.x);
            int ex=(int)(to.x-R*Math.cos(ang)), ey=(int)(to.y-R*Math.sin(ang));
            int sx=(int)(from.x+R*Math.cos(ang)), sy=(int)(from.y+R*Math.sin(ang));
            g2.drawLine(sx, sy, ex, ey);
            int al=11; double aa=Math.toRadians(25);
            g2.fillPolygon(
                new int[]{ex,(int)(ex-al*Math.cos(ang-aa)),(int)(ex-al*Math.cos(ang+aa))},
                new int[]{ey,(int)(ey-al*Math.sin(ang-aa)),(int)(ey-al*Math.sin(ang+aa))}, 3);
        }
    }

    // ========== JENDELA POPUP ==========
    static class GraphWindow extends JFrame {
        private GraphPanel panel;
        private JButton stepBtn, ulangBtn;
        private JLabel statusLabel;

        public GraphWindow(Graph g, java.util.List<String> traversal, String label) {
            setTitle("Graph — " + (label.isEmpty() ? "Visualisasi" : label));
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setLayout(new BorderLayout(4, 4));

            panel = new GraphPanel(g);
            if (traversal != null) panel.setTraversal(traversal, label);
            add(panel, BorderLayout.CENTER);

            // tombol step hanya kalau ada traversal
            if (traversal != null && !traversal.isEmpty()) {
                stepBtn  = new JButton("▶ Step");
                ulangBtn = new JButton("↺ Ulang");
                styleBtn(stepBtn,  new Color(60, 130, 190));
                styleBtn(ulangBtn, new Color(160, 65, 65));

                stepBtn.addActionListener(e -> {
                    if (panel.getStep() < panel.getTotalSteps() - 1) {
                        panel.nextStep();
                        int s = panel.getStep();
                        statusLabel.setText("Mengunjungi: " + panel.getCurrent() +
                            " (" + (s+1) + "/" + panel.getTotalSteps() + ")");
                        if (s == panel.getTotalSteps()-1) stepBtn.setEnabled(false);
                    }
                });
                ulangBtn.addActionListener(e -> {
                    panel.resetStep();
                    stepBtn.setEnabled(true);
                    statusLabel.setText("Siap. Tekan Step untuk mulai.");
                });

                statusLabel = new JLabel("Siap. Tekan Step untuk mulai.");
                statusLabel.setForeground(new Color(180, 210, 180));
                statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

                JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
                btnBar.setBackground(new Color(22, 28, 40));
                btnBar.add(ulangBtn);
                btnBar.add(stepBtn);
                btnBar.add(statusLabel);
                add(btnBar, BorderLayout.SOUTH);
            }

            pack();
            setLocationRelativeTo(null);
            setVisible(true);
        }

        private void styleBtn(JButton b, Color bg) {
            b.setBackground(bg); b.setForeground(Color.WHITE);
            b.setFont(new Font("SansSerif", Font.BOLD, 12));
            b.setBorderPainted(false); b.setFocusPainted(false);
            b.setPreferredSize(new Dimension(90, 28));
        }
    }

    // ========== MAIN (TERMINAL) ==========
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Pilih mode graph:");
        System.out.println("1. Undirected");
        System.out.println("2. Directed");
        System.out.print("Pilihan: ");
        int mode = sc.nextInt();
        Graph g = new Graph(mode == 2);
        System.out.println("Mode: " + (mode == 2 ? "Directed" : "Undirected"));

        int pilihan;
        do {
            System.out.println("\n=============================");
            System.out.println("       MENU GRAPH");
            System.out.println("=============================");
            System.out.println("1. Tambah Vertex");
            System.out.println("2. Hapus Vertex");
            System.out.println("3. Tambah Edge");
            System.out.println("4. Hapus Edge");
            System.out.println("5. Tampilkan Graph");
            System.out.println("6. Traversal DFS");
            System.out.println("7. Traversal BFS");
            System.out.println("8. Quit");
            System.out.println("=============================");
            System.out.print("Pilih menu: ");
            pilihan = sc.nextInt();

            switch (pilihan) {
                case 1:
                    System.out.print("Masukkan vertex: ");
                    g.tambahVertex(sc.next());
                    break;
                case 2:
                    System.out.print("Masukkan vertex yang dihapus: ");
                    g.hapusVertex(sc.next());
                    break;
                case 3:
                    System.out.print("Masukkan vertex asal: ");
                    String u3 = sc.next();
                    System.out.print("Masukkan vertex tujuan: ");
                    String v3 = sc.next();
                    g.tambahEdge(u3, v3);
                    break;
                case 4:
                    System.out.print("Masukkan vertex asal: ");
                    String u4 = sc.next();
                    System.out.print("Masukkan vertex tujuan: ");
                    String v4 = sc.next();
                    g.hapusEdge(u4, v4);
                    break;
                case 5:
                    g.tampilkanGraph();
                    showGraph(g, null, "");
                    break;
                case 6:
                    System.out.print("Masukkan vertex awal DFS: ");
                    String ds = sc.next().toUpperCase();
                    java.util.List<String> dfsResult = g.dfs(ds);
                    if (dfsResult == null) { System.out.println("Vertex tidak ditemukan!"); break; }
                    System.out.println("DFS dari " + ds + ": " + dfsResult);
                    showGraph(g, dfsResult, "DFS dari " + ds);
                    break;
                case 7:
                    System.out.print("Masukkan vertex awal BFS: ");
                    String bs = sc.next().toUpperCase();
                    java.util.List<String> bfsResult = g.bfs(bs);
                    if (bfsResult == null) { System.out.println("Vertex tidak ditemukan!"); break; }
                    System.out.println("BFS dari " + bs + ": " + bfsResult);
                    showGraph(g, bfsResult, "BFS dari " + bs);
                    break;
                case 8:
                    System.out.println("Terima kasih! Program selesai.");
                    break;
                default:
                    System.out.println("Pilihan tidak valid!");
            }
        } while (pilihan != 8);

        sc.close();
        System.exit(0);
    }

    static void showGraph(Graph g, java.util.List<String> traversal, String label) {
        SwingUtilities.invokeLater(() -> new GraphWindow(g, traversal, label));
    }
}
