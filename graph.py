import tkinter as tk
from collections import deque
import math
import threading

class Graph:
    def __init__(self, directed: bool):
        self.directed = directed
        self.vertices: list[str] = []
        self.adj: dict[str, list[str]] = {}

    # 1. Tambah Vertex
    def tambah_vertex(self, v: str) -> str:
        v = v.upper()
        if v in self.adj:
            return f"Vertex {v} sudah ada!"
        self.vertices.append(v)
        self.adj[v] = []
        return f"Vertex {v} berhasil ditambahkan."

    # 2. Hapus Vertex
    def hapus_vertex(self, v: str) -> str:
        v = v.upper()
        if v not in self.adj:
            return f"Vertex {v} tidak ditemukan!"
        self.vertices.remove(v)
        del self.adj[v]
        for u in self.vertices:
            if v in self.adj[u]:
                self.adj[u].remove(v)
        return f"Vertex {v} berhasil dihapus."

    # 3. Tambah Edge
    def tambah_edge(self, u: str, v: str) -> str:
        u, v = u.upper(), v.upper()
        if u not in self.adj or v not in self.adj:
            return "Salah satu atau kedua vertex tidak ditemukan!"
        arrow = " -> " if self.directed else " - "
        if v in self.adj[u]:
            return f"Edge {u}{arrow}{v} sudah ada!"
        self.adj[u].append(v)
        if not self.directed:
            self.adj[v].append(u)
        return f"Edge {u}{arrow}{v} berhasil ditambahkan."

    # 4. Hapus Edge
    def hapus_edge(self, u: str, v: str) -> str:
        u, v = u.upper(), v.upper()
        if u not in self.adj or v not in self.adj:
            return "Salah satu atau kedua vertex tidak ditemukan!"
        arrow = " -> " if self.directed else " - "
        if v not in self.adj[u]:
            return f"Edge {u}{arrow}{v} tidak ditemukan!"
        self.adj[u].remove(v)
        if not self.directed:
            self.adj[v].remove(u)
        return f"Edge {u}{arrow}{v} berhasil dihapus."

    # 5. Tampilkan graph (teks terminal)
    def tampilkan_graph(self):
        if not self.vertices:
            print("Graph kosong!")
            return
        vlist = sorted(self.vertices)
        print("\n=== ADJACENCY LIST ===")
        for v in vlist:
            nb = sorted(self.adj[v])
            print(f"Vertex {v} -> {nb if nb else '(tidak ada tetangga)'}")
        print("\n=== ADJACENCY MATRIX ===")
        print("    " + "".join(f"{v:>4}" for v in vlist))
        for u in vlist:
            row = f"{u:>4}" + "".join(f"{'1':>4}" if v in self.adj[u] else f"{'0':>4}" for v in vlist)
            print(row)

    # 6. DFS
    def dfs(self, start: str) -> list[str] | None:
        start = start.upper()
        if start not in self.adj:
            return None
        result, visited = [], set()
        def _dfs(v):
            visited.add(v); result.append(v)
            for n in sorted(self.adj[v]):
                if n not in visited:
                    _dfs(n)
        _dfs(start)
        return result

    # 7. BFS
    def bfs(self, start: str) -> list[str] | None:
        start = start.upper()
        if start not in self.adj:
            return None
        result, visited = [], {start}
        queue = deque([start])
        while queue:
            v = queue.popleft(); result.append(v)
            for n in sorted(self.adj[v]):
                if n not in visited:
                    visited.add(n); queue.append(n)
        return result


class GraphPanel(tk.Canvas):
    R      = 26
    C_BG   = "#1e2330"
    C_NODE = "#64afdc"
    C_VIS  = "#50c878"
    C_CUR  = "#ffc83c"
    C_EDGE = "#b4c8dc"
    C_TEXT = "#141e28"

    def __init__(self, master, graph: Graph, **kw):
        super().__init__(master, bg=self.C_BG, **kw)
        self.graph = graph
        self.pos: dict[str, tuple[int, int]] = {}
        self.traversal: list[str] = []
        self.step = -1
        self.label = ""
        self.layout()

    def layout(self):
        self.pos.clear()
        vlist = list(self.graph.vertices)
        n = len(vlist)
        if n == 0:
            return
        cx, cy, rad = 300, 225, 160
        if n == 1:
            self.pos[vlist[0]] = (cx, cy)
            return
        for i, v in enumerate(vlist):
            a = 2 * math.pi * i / n - math.pi / 2
            self.pos[v] = (int(cx + rad * math.cos(a)), int(cy + rad * math.sin(a)))

    def set_traversal(self, t: list[str], lbl: str):
        self.traversal = list(t); self.label = lbl; self.step = -1
        self.draw()

    def next_step(self):
        if self.step < len(self.traversal) - 1:
            self.step += 1; self.draw()

    def reset_step(self):
        self.step = -1; self.draw()

    def current(self) -> str:
        return self.traversal[self.step] if self.step >= 0 else ""

    def draw(self):
        self.delete("all")
        g = self.graph
        W, H = 600, 450

        title = ("Directed" if g.directed else "Undirected") + " Graph"
        if self.label:
            title += f"  —  {self.label}"
        self.create_text(W // 2, 22, text=title,
                         fill="#d2e1f5", font=("Helvetica", 13, "bold"))

        if not g.vertices:
            self.create_text(W // 2, H // 2, text="Graph kosong.",
                             fill="#788090", font=("Helvetica", 12))
            return

        visited = set(self.traversal[:self.step + 1]) if self.step >= 0 else set()
        cur = self.current()

        # edges
        drawn = set()
        for u in g.vertices:
            if u not in self.pos: continue
            pu = self.pos[u]
            for v in g.adj[u]:
                if v not in self.pos: continue
                pv = self.pos[v]
                key = f"{u}->{v}" if g.directed else (f"{u}-{v}" if u < v else f"{v}-{u}")
                if key in drawn: continue
                drawn.add(key)
                if g.directed:
                    self._draw_arrow(pu, pv)
                else:
                    self.create_line(pu[0], pu[1], pv[0], pv[1],
                                     fill=self.C_EDGE, width=2)

        # nodes
        for v in g.vertices:
            if v not in self.pos: continue
            px, py = self.pos[v]
            r = self.R
            color = self.C_CUR if v == cur else (self.C_VIS if v in visited else self.C_NODE)
            self.create_oval(px-r, py-r, px+r, py+r, fill=color,
                             outline=self._darker(color), width=2)
            # nomor langkah
            if v in self.traversal:
                idx = self.traversal.index(v)
                if 0 <= idx <= self.step:
                    bx, by = px + r - 8, py - r - 10
                    self.create_oval(bx, by, bx+17, by+17, fill="#14141e", outline="")
                    self.create_text(bx+8, by+8, text=str(idx+1),
                                     fill="white", font=("Helvetica", 9, "bold"))
            # label vertex
            self.create_text(px, py, text=v,
                             fill=self.C_TEXT, font=("Helvetica", 14, "bold"))

        # info langkah traversal
        if self.traversal and self.step >= 0:
            info = (f"Langkah {self.step+1}/{len(self.traversal)}"
                    f"  |  Urutan: {self.traversal[:self.step+1]}")
            self.create_text(10, H - 10, anchor="sw", text=info,
                             fill="#b4d2b4", font=("Helvetica", 11))

    def _draw_arrow(self, frm, to):
        fx, fy = frm; tx, ty = to
        ang = math.atan2(ty - fy, tx - fx)
        r = self.R
        ex = int(tx - r * math.cos(ang)); ey = int(ty - r * math.sin(ang))
        sx = int(fx + r * math.cos(ang)); sy = int(fy + r * math.sin(ang))
        self.create_line(sx, sy, ex, ey, fill=self.C_EDGE, width=2,
                         arrow=tk.LAST, arrowshape=(12, 14, 5))

    @staticmethod
    def _darker(hex_color: str) -> str:
        r = max(0, int(hex_color[1:3], 16) - 40)
        g = max(0, int(hex_color[3:5], 16) - 40)
        b = max(0, int(hex_color[5:7], 16) - 40)
        return f"#{r:02x}{g:02x}{b:02x}"


class GraphWindow(tk.Toplevel):
    def __init__(self, master, graph: Graph,
                 traversal: list[str] | None, label: str):
        super().__init__(master)
        self.title(f"Graph — {label if label else 'Visualisasi'}")
        self.configure(bg="#161c28")
        self.resizable(False, False)

        self.panel = GraphPanel(self, graph, width=600, height=450)
        self.panel.pack(padx=8, pady=8)

        if traversal:
            self.panel.set_traversal(traversal, label)

            btn_bar = tk.Frame(self, bg="#161c28")
            btn_bar.pack(fill="x", pady=(0, 8))

            self.status = tk.Label(btn_bar, text="Siap. Tekan ▶ untuk mulai.",
                                   fg="#b4d2b4", bg="#161c28",
                                   font=("Helvetica", 11))

            def step_fn():
                if self.panel.step < len(self.panel.traversal) - 1:
                    self.panel.next_step()
                    s = self.panel.step
                    self.status.config(
                        text=f"Mengunjungi: {self.panel.current()} "
                             f"({s+1}/{len(self.panel.traversal)})")
                    if s == len(self.panel.traversal) - 1:
                        btn_step.config(state="disabled")

            def restart_fn():
                self.panel.reset_step()
                btn_step.config(state="normal")
                self.status.config(text="Siap. Tekan ▶ untuk mulai.")

            btn_restart = tk.Button(btn_bar, text="↺ Restart",
                                    command=restart_fn, **_btn(("#a04040")))
            btn_step    = tk.Button(btn_bar, text="▶ Start",
                                    command=step_fn,    **_btn(("#3c82be")))

            btn_restart.pack(side="left", padx=(80, 8))
            btn_step.pack(side="left", padx=4)
            self.status.pack(side="left", padx=16)
        else:
            self.panel.layout()
            self.panel.draw()


def _btn(bg: str) -> dict:
    return dict(bg=bg, fg="white", font=("Helvetica", 11, "bold"),
                relief="flat", padx=10, pady=4, cursor="hand2",
                activebackground=bg, activeforeground="white")

_root: tk.Tk | None = None

def _ensure_root():
    global _root
    if _root is None:
        _root = tk.Tk()
        _root.withdraw()   # sembunyikan jendela root

def show_graph(graph: Graph,
               traversal: list[str] | None = None,
               label: str = ""):
    """Buka jendela visualisasi dari thread terminal."""
    _ensure_root()
    def _open():
        w = GraphWindow(_root, graph, traversal, label)
        w.focus_force()
    _root.after(0, _open)


def _run_tk():
    """Jalankan event-loop tkinter di thread terpisah."""
    _ensure_root()
    _root.mainloop()


def main():
    # Jalankan tkinter di background thread
    tk_thread = threading.Thread(target=_run_tk, daemon=True)
    tk_thread.start()

    # ── Pilih mode ──
    print("Pilih mode graph:")
    print("1. Undirected")
    print("2. Directed")
    while True:
        try:
            mode = int(input("Pilihan: "))
            if mode in (1, 2):
                break
            print("Masukkan 1 atau 2!")
        except ValueError:
            print("Masukkan angka!")

    g = Graph(directed=(mode == 2))
    print(f"Mode: {'Directed' if g.directed else 'Undirected'}")

    while True:
        print("\n=============================")
        print("       MENU GRAPH")
        print("=============================")
        print("1. Tambah Vertex")
        print("2. Hapus Vertex")
        print("3. Tambah Edge")
        print("4. Hapus Edge")
        print("5. Tampilkan Graph")
        print("6. Traversal DFS")
        print("7. Traversal BFS")
        print("8. Quit")
        print("=============================")

        try:
            pilihan = int(input("Pilih menu: "))
        except ValueError:
            print("Masukkan angka!")
            continue

        if pilihan == 1:
            v = input("Masukkan vertex: ")
            print(g.tambah_vertex(v))

        elif pilihan == 2:
            v = input("Masukkan vertex yang dihapus: ")
            print(g.hapus_vertex(v))

        elif pilihan == 3:
            u = input("Masukkan vertex asal: ")
            v = input("Masukkan vertex tujuan: ")
            print(g.tambah_edge(u, v))

        elif pilihan == 4:
            u = input("Masukkan vertex asal: ")
            v = input("Masukkan vertex tujuan: ")
            print(g.hapus_edge(u, v))

        elif pilihan == 5:
            g.tampilkan_graph()
            show_graph(g)

        elif pilihan == 6:
            s = input("Masukkan vertex awal DFS: ").upper()
            result = g.dfs(s)
            if result is None:
                print("Vertex tidak ditemukan!")
            else:
                print(f"DFS dari {s}: {result}")
                show_graph(g, result, f"DFS dari {s}")

        elif pilihan == 7:
            s = input("Masukkan vertex awal BFS: ").upper()
            result = g.bfs(s)
            if result is None:
                print("Vertex tidak ditemukan!")
            else:
                print(f"BFS dari {s}: {result}")
                show_graph(g, result, f"BFS dari {s}")

        elif pilihan == 8:
            print("Terima kasih! Program selesai.")
            break

        else:
            print("Pilihan tidak valid!")

    if _root:
        _root.after(0, _root.destroy)


if __name__ == "__main__":
    main()
