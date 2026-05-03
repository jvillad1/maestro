import { useState, useEffect, useRef, useCallback } from "react";

// ============================================================
// MAESTRO — App para Profesores de Música
// Design: Warm luxury meets editorial precision
// Palette: Deep cream #F5F0E8, Espresso #2C1810, Gold #C9A84C,
//          Forest #2D5016, Terracotta #B85C38, Soft White #FDFAF5
// ============================================================

const PALETTE = {
  cream: "#F5F0E8",
  espresso: "#2C1810",
  gold: "#C9A84C",
  forest: "#2D5016",
  terra: "#B85C38",
  white: "#FDFAF5",
  muted: "#8B7355",
  lightGold: "#F0E6C8",
  softGreen: "#E8F0E0",
};

const MOTIVATIONAL_PHRASES = [
  { text: "Enseñar música es sembrar semillas que florecen por generaciones.", author: "Naty Ramírez" },
  { text: "Cada clase es una obra maestra que nunca volverá a repetirse.", author: "Maestro anónimo" },
  { text: "La paciencia del maestro es el compás que guía el alma del estudiante.", author: "Heinrich Neuhaus" },
  { text: "No enseñas notas. Enseñas a escuchar el universo.", author: "Nadia Boulanger" },
  { text: "Tu energía de hoy es el recuerdo musical de alguien mañana.", author: "Naty Ramírez" },
  { text: "Un buen maestro abre puertas que el estudiante ni sabía que existían.", author: "Leon Fleisher" },
  { text: "Cuídate. Un maestro agotado no puede encender llamas en otros.", author: "Reflexión docente" },
  { text: "La excelencia no es perfección. Es presencia total en cada compás.", author: "Naty Ramírez" },
];

const LEVEL_GUIDE = {
  "Inicial": {
    color: "#7BC67E",
    objectives: ["Postura y posición al piano", "Lectura de notas básicas (Do-Sol)", "Ritmo con valores básicos", "Canciones simples con 5 dedos"],
    duration: "6-12 meses",
    repertoire: ["Pequeñas piezas de Bartók", "Canciones folclóricas adaptadas", "Método Suzuki Vol.1"]
  },
  "Elemental": {
    color: "#64B5F6",
    objectives: ["Escalas mayores (Do, Sol, Re)", "Lectura en clave de Fa", "Articulaciones básicas", "Piezas de un nivel ABRSM 1-2"],
    duration: "1-2 años",
    repertoire: ["Bach Anna Magdalena", "Clementi Sonatinas Op.36", "Bartók Mikrokosmos Vol.1-2"]
  },
  "Intermedio": {
    color: "#FFB74D",
    objectives: ["Escalas y arpegios completos", "Fraseo musical", "Pedal de resonancia", "Análisis formal básico"],
    duration: "2-3 años",
    repertoire: ["Mozart Sonatas sencillas", "Beethoven Sonatinas", "Chopin Preludios fáciles"]
  },
  "Avanzado": {
    color: "#F06292",
    objectives: ["Técnica avanzada", "Repertorio de concierto", "Interpretación estilística", "Preparación para audiciones"],
    duration: "3+ años",
    repertoire: ["Bach Invenciones / Partitas", "Beethoven Sonatas Op.13, 27", "Chopin Baladas, Nocturnos"]
  }
};

// ============================================================
// UTILITIES
// ============================================================
const formatCurrency = (n) => `$${Number(n).toLocaleString("es-CO")}`;
const today = new Date();
const monthNames = ["Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"];

// ============================================================
// MAIN APP
// ============================================================
export default function MaestroApp() {
  const [activeTab, setActiveTab] = useState("dashboard");
  const [students, setStudents] = useState([
    { id: 1, name: "Isabella García", age: 10, level: "Elemental", phone: "300-123-4567", email: "isabella@gmail.com", monthlyFee: 250000, notes: "Muy dedicada. Trabaja escalas en casa.", joinDate: "2024-01-15", color: "#C9A84C" },
    { id: 2, name: "Mateo Restrepo", age: 14, level: "Intermedio", phone: "301-234-5678", email: "mateo@gmail.com", monthlyFee: 320000, notes: "Necesita trabajar la independencia de manos.", joinDate: "2023-08-20", color: "#B85C38" },
    { id: 3, name: "Valentina López", age: 8, level: "Inicial", phone: "302-345-6789", email: "valentina@gmail.com", monthlyFee: 220000, notes: "Aprende muy rápido. Excelente oído.", joinDate: "2024-03-01", color: "#2D5016" },
  ]);
  const [classes, setClasses] = useState([
    { id: 1, studentId: 1, date: `${today.getFullYear()}-${String(today.getMonth()+1).padStart(2,'0')}-05`, topic: "Escala de Sol Mayor + Inventio BWV 772", paid: true },
    { id: 2, studentId: 2, date: `${today.getFullYear()}-${String(today.getMonth()+1).padStart(2,'0')}-08`, topic: "Sonatina Op.36 N°1 - sección desarrollo", paid: true },
    { id: 3, studentId: 3, date: `${today.getFullYear()}-${String(today.getMonth()+1).padStart(2,'0')}-10`, topic: "Lectura de notas en pentagrama", paid: false },
    { id: 4, studentId: 1, date: `${today.getFullYear()}-${String(today.getMonth()+1).padStart(2,'0')}-12`, topic: "Lectura a primera vista + Hanon", paid: true },
  ]);
  const [tasks, setTasks] = useState([
    { id: 1, text: "Preparar repertorio para recital de junio", done: false, priority: "alta" },
    { id: 2, text: "Enviar recibos de pago del mes", done: false, priority: "alta" },
    { id: 3, text: "Buscar método para Valentina (nivel inicial avanzado)", done: false, priority: "media" },
    { id: 4, text: "Revisar grabaciones del curso SONUS PRO", done: true, priority: "baja" },
  ]);
  const [events, setEvents] = useState([
    { id: 1, title: "Recital Fin de Semestre", date: `${today.getFullYear()}-06-15`, type: "recital", description: "Auditorio PIANOACADEMY - 6pm" },
    { id: 2, title: "Masterclass con Inv. Especial", date: `${today.getFullYear()}-05-25`, type: "masterclass", description: "Registro abierto para estudiantes avanzados" },
    { id: 3, title: "Evaluación de nivel", date: `${today.getFullYear()}-05-30`, type: "evaluacion", description: "Todos los estudiantes, 2-6pm" },
  ]);

  const [selectedStudent, setSelectedStudent] = useState(null);
  const [currentPhrase, setCurrentPhrase] = useState(0);
  const [waterReminder, setWaterReminder] = useState(false);
  const [waterCount, setWaterCount] = useState(0);
  const [showAddStudent, setShowAddStudent] = useState(false);
  const [showAddClass, setShowAddClass] = useState(false);
  const [showAddTask, setShowAddTask] = useState(false);
  const [showAddEvent, setShowAddEvent] = useState(false);
  const [newStudent, setNewStudent] = useState({ name:"", age:"", level:"Inicial", phone:"", email:"", monthlyFee:"", notes:"" });
  const [newClass, setNewClass] = useState({ studentId:"", date:"", topic:"", paid:false });
  const [newTask, setNewTask] = useState({ text:"", priority:"media" });
  const [newEvent, setNewEvent] = useState({ title:"", date:"", type:"recital", description:"" });

  // Water reminder every 45min
  useEffect(() => {
    const timer = setInterval(() => setWaterReminder(true), 45 * 60 * 1000);
    return () => clearInterval(timer);
  }, []);

  // Phrase rotation
  useEffect(() => {
    const timer = setInterval(() => setCurrentPhrase(p => (p+1) % MOTIVATIONAL_PHRASES.length), 8000);
    return () => clearInterval(timer);
  }, []);

  const currentMonth = today.getMonth();
  const currentYear = today.getFullYear();
  const monthClasses = classes.filter(c => {
    const d = new Date(c.date);
    return d.getMonth() === currentMonth && d.getFullYear() === currentYear;
  });
  const totalMonthIncome = students.reduce((acc, s) => {
    const sc = monthClasses.filter(c => c.studentId === s.id && c.paid);
    return acc + (sc.length > 0 ? s.monthlyFee : 0);
  }, 0);
  const pendingAmount = students.reduce((acc, s) => {
    const sc = monthClasses.filter(c => c.studentId === s.id && !c.paid);
    return acc + (sc.length > 0 ? s.monthlyFee : 0);
  }, 0);

  const addStudent = () => {
    if (!newStudent.name) return;
    const colors = ["#C9A84C","#B85C38","#2D5016","#7B68EE","#E67E22","#16A085","#8E44AD"];
    setStudents(prev => [...prev, { ...newStudent, id: Date.now(), age: Number(newStudent.age), monthlyFee: Number(newStudent.monthlyFee), joinDate: new Date().toISOString().split("T")[0], color: colors[prev.length % colors.length] }]);
    setNewStudent({ name:"", age:"", level:"Inicial", phone:"", email:"", monthlyFee:"", notes:"" });
    setShowAddStudent(false);
  };

  const addClassEntry = () => {
    if (!newClass.studentId || !newClass.date) return;
    setClasses(prev => [...prev, { ...newClass, id: Date.now(), studentId: Number(newClass.studentId), paid: newClass.paid === "true" || newClass.paid === true }]);
    setNewClass({ studentId:"", date:"", topic:"", paid:false });
    setShowAddClass(false);
  };

  const addTask = () => {
    if (!newTask.text) return;
    setTasks(prev => [...prev, { ...newTask, id: Date.now(), done: false }]);
    setNewTask({ text:"", priority:"media" });
    setShowAddTask(false);
  };

  const addEvent = () => {
    if (!newEvent.title || !newEvent.date) return;
    setEvents(prev => [...prev, { ...newEvent, id: Date.now() }]);
    setNewEvent({ title:"", date:"", type:"recital", description:"" });
    setShowAddEvent(false);
  };

  const toggleTask = (id) => setTasks(prev => prev.map(t => t.id === id ? {...t, done: !t.done} : t));
  const deleteTask = (id) => setTasks(prev => prev.filter(t => t.id !== id));

  const tabs = [
    { id: "dashboard", label: "Inicio", icon: "♪" },
    { id: "students", label: "Estudiantes", icon: "♬" },
    { id: "classes", label: "Clases", icon: "📋" },
    { id: "finances", label: "Finanzas", icon: "₿" },
    { id: "tasks", label: "Tareas", icon: "✓" },
    { id: "calendar", label: "Agenda", icon: "📅" },
    { id: "metronome", label: "Metrónomo", icon: "𝅗𝅥" },
    { id: "guide", label: "Guía", icon: "📖" },
  ];

  return (
    <div style={{ fontFamily: "'Georgia', 'Times New Roman', serif", background: PALETTE.cream, minHeight: "100vh", color: PALETTE.espresso }}>
      {/* WATER REMINDER TOAST */}
      {waterReminder && (
        <div style={{ position:"fixed", top:20, right:20, zIndex:9999, background: PALETTE.espresso, color: PALETTE.white, padding:"16px 24px", borderRadius:12, boxShadow:"0 8px 32px rgba(0,0,0,0.3)", maxWidth:280, animation: "slideIn 0.3s ease" }}>
          <div style={{ fontSize:28, textAlign:"center", marginBottom:8 }}>💧</div>
          <div style={{ fontWeight:700, marginBottom:4, fontFamily:"sans-serif" }}>¡Hora de hidratarte!</div>
          <div style={{ fontSize:13, opacity:0.8, fontFamily:"sans-serif", marginBottom:12 }}>Llevas más de 45 minutos enseñando. Un sorbo de agua renueva tu energía.</div>
          <button onClick={() => { setWaterReminder(false); setWaterCount(w=>w+1); }} style={{ background: PALETTE.gold, border:"none", color: PALETTE.espresso, padding:"8px 16px", borderRadius:6, cursor:"pointer", fontWeight:700, width:"100%", fontFamily:"sans-serif" }}>
            ¡Bebí agua! ({waterCount + 1})
          </button>
        </div>
      )}

      {/* HEADER */}
      <header style={{ background: PALETTE.espresso, color: PALETTE.white, padding:"0 24px", display:"flex", alignItems:"center", justifyContent:"space-between", height:64, position:"sticky", top:0, zIndex:100, boxShadow:"0 2px 20px rgba(0,0,0,0.3)" }}>
        <div style={{ display:"flex", alignItems:"center", gap:12 }}>
          <div style={{ width:36, height:36, background: `linear-gradient(135deg, ${PALETTE.gold}, #E8C87A)`, borderRadius:"50%", display:"flex", alignItems:"center", justifyContent:"center", fontSize:18 }}>♪</div>
          <div>
            <div style={{ fontWeight:700, fontSize:18, letterSpacing:2, fontFamily:"sans-serif" }}>MAESTRO</div>
            <div style={{ fontSize:10, opacity:0.6, letterSpacing:3, fontFamily:"sans-serif", textTransform:"uppercase" }}>Para Profesores de Música</div>
          </div>
        </div>
        <div style={{ display:"flex", alignItems:"center", gap:16 }}>
          <div style={{ textAlign:"right", display:"none" }}>
            <div style={{ fontSize:12, opacity:0.7, fontFamily:"sans-serif" }}>{monthNames[currentMonth]} {currentYear}</div>
          </div>
          <button onClick={() => setWaterReminder(true)} style={{ background:"rgba(201,168,76,0.2)", border:`1px solid ${PALETTE.gold}`, color: PALETTE.gold, padding:"6px 12px", borderRadius:20, cursor:"pointer", fontSize:13, fontFamily:"sans-serif", display:"flex", alignItems:"center", gap:6 }}>
            💧 {waterCount}
          </button>
        </div>
      </header>

      {/* NAV */}
      <nav style={{ background: PALETTE.white, borderBottom:`2px solid ${PALETTE.lightGold}`, overflowX:"auto", display:"flex", padding:"0 12px" }}>
        {tabs.map(t => (
          <button key={t.id} onClick={() => { setActiveTab(t.id); setSelectedStudent(null); }} style={{ padding:"14px 16px", border:"none", background:"transparent", cursor:"pointer", fontFamily:"sans-serif", fontSize:13, color: activeTab === t.id ? PALETTE.terra : PALETTE.muted, borderBottom: activeTab === t.id ? `3px solid ${PALETTE.terra}` : "3px solid transparent", fontWeight: activeTab === t.id ? 700 : 400, transition:"all 0.2s", whiteSpace:"nowrap", display:"flex", alignItems:"center", gap:6 }}>
            <span style={{ fontSize:16 }}>{t.icon}</span> {t.label}
          </button>
        ))}
      </nav>

      {/* CONTENT */}
      <main style={{ maxWidth:1100, margin:"0 auto", padding:"24px 20px" }}>

        {/* ===== DASHBOARD ===== */}
        {activeTab === "dashboard" && (
          <div>
            {/* Motivational phrase */}
            <div style={{ background:`linear-gradient(135deg, ${PALETTE.espresso}, #4A2C1A)`, color: PALETTE.white, borderRadius:16, padding:"28px 32px", marginBottom:24, position:"relative", overflow:"hidden" }}>
              <div style={{ position:"absolute", right:20, top:10, opacity:0.05, fontSize:120, fontFamily:"serif" }}>♫</div>
              <div style={{ fontSize:11, letterSpacing:3, color: PALETTE.gold, marginBottom:12, fontFamily:"sans-serif", textTransform:"uppercase" }}>Frase del día</div>
              <div style={{ fontSize:20, fontStyle:"italic", lineHeight:1.6, marginBottom:12, maxWidth:600 }}>"{MOTIVATIONAL_PHRASES[currentPhrase].text}"</div>
              <div style={{ fontSize:13, color: PALETTE.gold, fontFamily:"sans-serif" }}>— {MOTIVATIONAL_PHRASES[currentPhrase].author}</div>
              <div style={{ display:"flex", gap:6, marginTop:16 }}>
                {MOTIVATIONAL_PHRASES.map((_, i) => (
                  <div key={i} onClick={() => setCurrentPhrase(i)} style={{ width: i === currentPhrase ? 24 : 6, height:6, borderRadius:3, background: i === currentPhrase ? PALETTE.gold : "rgba(255,255,255,0.3)", cursor:"pointer", transition:"width 0.3s" }} />
                ))}
              </div>
            </div>

            {/* Stats row */}
            <div style={{ display:"grid", gridTemplateColumns:"repeat(auto-fit, minmax(180px, 1fr))", gap:16, marginBottom:24 }}>
              {[
                { label:"Estudiantes activos", value: students.length, icon:"♬", color: PALETTE.forest, bg: PALETTE.softGreen },
                { label:"Clases este mes", value: monthClasses.length, icon:"📋", color: PALETTE.terra, bg:"#FAE8E0" },
                { label:"Ingreso cobrado", value: formatCurrency(totalMonthIncome), icon:"✓", color: PALETTE.forest, bg: PALETTE.softGreen },
                { label:"Por cobrar", value: formatCurrency(pendingAmount), icon:"⏳", color: "#C0392B", bg:"#FDECEA" },
                { label:"Tareas pendientes", value: tasks.filter(t=>!t.done).length, icon:"☐", color: PALETTE.espresso, bg: PALETTE.lightGold },
              ].map((s,i) => (
                <div key={i} style={{ background: s.bg, borderRadius:12, padding:"20px", border:`1px solid rgba(0,0,0,0.06)` }}>
                  <div style={{ fontSize:24, marginBottom:8 }}>{s.icon}</div>
                  <div style={{ fontSize:22, fontWeight:700, color: s.color, fontFamily:"sans-serif" }}>{s.value}</div>
                  <div style={{ fontSize:12, color: PALETTE.muted, fontFamily:"sans-serif", marginTop:4 }}>{s.label}</div>
                </div>
              ))}
            </div>

            {/* Recent classes + upcoming events */}
            <div style={{ display:"grid", gridTemplateColumns:"1fr 1fr", gap:20 }}>
              <div style={{ background: PALETTE.white, borderRadius:12, padding:20, border:`1px solid ${PALETTE.lightGold}` }}>
                <div style={{ fontSize:14, fontWeight:700, fontFamily:"sans-serif", marginBottom:16, color: PALETTE.espresso, letterSpacing:1, textTransform:"uppercase" }}>Últimas Clases</div>
                {classes.slice(-4).reverse().map(c => {
                  const s = students.find(st => st.id === c.studentId);
                  return (
                    <div key={c.id} style={{ display:"flex", alignItems:"center", gap:12, padding:"10px 0", borderBottom:`1px solid ${PALETTE.lightGold}` }}>
                      <div style={{ width:8, height:8, borderRadius:"50%", background: s?.color || PALETTE.gold, flexShrink:0 }} />
                      <div style={{ flex:1 }}>
                        <div style={{ fontFamily:"sans-serif", fontSize:13, fontWeight:600 }}>{s?.name}</div>
                        <div style={{ fontFamily:"sans-serif", fontSize:11, color: PALETTE.muted }}>{c.topic}</div>
                      </div>
                      <div style={{ fontSize:11, fontFamily:"sans-serif", color: c.paid ? PALETTE.forest : PALETTE.terra, fontWeight:700 }}>{c.paid ? "✓ Pagado" : "⏳ Pendiente"}</div>
                    </div>
                  );
                })}
              </div>

              <div style={{ background: PALETTE.white, borderRadius:12, padding:20, border:`1px solid ${PALETTE.lightGold}` }}>
                <div style={{ fontSize:14, fontWeight:700, fontFamily:"sans-serif", marginBottom:16, color: PALETTE.espresso, letterSpacing:1, textTransform:"uppercase" }}>Próximos Eventos</div>
                {events.sort((a,b) => new Date(a.date)-new Date(b.date)).slice(0,4).map(ev => {
                  const typeColors = { recital: PALETTE.terra, masterclass: PALETTE.forest, evaluacion: "#7B68EE", otro: PALETTE.muted };
                  const d = new Date(ev.date + "T12:00:00");
                  return (
                    <div key={ev.id} style={{ display:"flex", alignItems:"center", gap:12, padding:"10px 0", borderBottom:`1px solid ${PALETTE.lightGold}` }}>
                      <div style={{ background: typeColors[ev.type] || PALETTE.gold, borderRadius:8, padding:"6px 8px", textAlign:"center", minWidth:48 }}>
                        <div style={{ fontSize:11, color:"white", fontFamily:"sans-serif", fontWeight:700 }}>{d.getDate()}</div>
                        <div style={{ fontSize:9, color:"rgba(255,255,255,0.8)", fontFamily:"sans-serif" }}>{monthNames[d.getMonth()].slice(0,3)}</div>
                      </div>
                      <div>
                        <div style={{ fontFamily:"sans-serif", fontSize:13, fontWeight:600 }}>{ev.title}</div>
                        <div style={{ fontFamily:"sans-serif", fontSize:11, color: PALETTE.muted }}>{ev.description}</div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Quick tasks */}
            <div style={{ background: PALETTE.white, borderRadius:12, padding:20, border:`1px solid ${PALETTE.lightGold}`, marginTop:20 }}>
              <div style={{ fontSize:14, fontWeight:700, fontFamily:"sans-serif", marginBottom:16, color: PALETTE.espresso, letterSpacing:1, textTransform:"uppercase", display:"flex", justifyContent:"space-between", alignItems:"center" }}>
                Tareas Pendientes
                <button onClick={() => setActiveTab("tasks")} style={{ fontSize:11, color: PALETTE.terra, border:"none", background:"none", cursor:"pointer", fontFamily:"sans-serif" }}>Ver todas →</button>
              </div>
              {tasks.filter(t => !t.done).slice(0,3).map(t => (
                <div key={t.id} style={{ display:"flex", gap:10, alignItems:"center", padding:"8px 0", borderBottom:`1px solid ${PALETTE.lightGold}` }}>
                  <input type="checkbox" checked={t.done} onChange={() => toggleTask(t.id)} style={{ accentColor: PALETTE.terra, width:16, height:16 }} />
                  <span style={{ fontFamily:"sans-serif", fontSize:13, flex:1 }}>{t.text}</span>
                  <span style={{ fontSize:10, padding:"2px 8px", borderRadius:10, background: t.priority === "alta" ? "#FDECEA" : t.priority === "media" ? PALETTE.lightGold : "#E8F5E9", color: t.priority === "alta" ? "#C0392B" : t.priority === "media" ? PALETTE.espresso : PALETTE.forest, fontFamily:"sans-serif", fontWeight:700 }}>{t.priority}</span>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* ===== STUDENTS ===== */}
        {activeTab === "students" && !selectedStudent && (
          <div>
            <div style={{ display:"flex", justifyContent:"space-between", alignItems:"center", marginBottom:24 }}>
              <div>
                <h1 style={{ fontSize:28, margin:0, fontWeight:400 }}>Mis Estudiantes</h1>
                <p style={{ color: PALETTE.muted, fontFamily:"sans-serif", fontSize:13, margin:"4px 0 0" }}>{students.length} estudiantes activos</p>
              </div>
              <button onClick={() => setShowAddStudent(true)} style={{ background: PALETTE.terra, color: PALETTE.white, border:"none", padding:"12px 20px", borderRadius:8, cursor:"pointer", fontFamily:"sans-serif", fontWeight:600, fontSize:14 }}>+ Nuevo Estudiante</button>
            </div>

            <div style={{ display:"grid", gridTemplateColumns:"repeat(auto-fill, minmax(300px, 1fr))", gap:16 }}>
              {students.map(s => {
                const sc = monthClasses.filter(c => c.studentId === s.id);
                const paidClasses = sc.filter(c => c.paid);
                return (
                  <div key={s.id} onClick={() => setSelectedStudent(s)} style={{ background: PALETTE.white, borderRadius:14, padding:24, border:`1px solid ${PALETTE.lightGold}`, cursor:"pointer", transition:"transform 0.2s, box-shadow 0.2s", borderLeft:`4px solid ${s.color}` }}
                    onMouseEnter={e => { e.currentTarget.style.transform = "translateY(-2px)"; e.currentTarget.style.boxShadow = "0 8px 24px rgba(0,0,0,0.1)"; }}
                    onMouseLeave={e => { e.currentTarget.style.transform = "translateY(0)"; e.currentTarget.style.boxShadow = "none"; }}>
                    <div style={{ display:"flex", justifyContent:"space-between", alignItems:"flex-start", marginBottom:12 }}>
                      <div style={{ width:48, height:48, borderRadius:"50%", background:`linear-gradient(135deg, ${s.color}, ${s.color}88)`, display:"flex", alignItems:"center", justifyContent:"center", fontSize:20, color:"white", fontWeight:700, fontFamily:"sans-serif" }}>{s.name[0]}</div>
                      <span style={{ background: LEVEL_GUIDE[s.level]?.color || PALETTE.gold, color:"white", padding:"4px 10px", borderRadius:12, fontSize:11, fontFamily:"sans-serif", fontWeight:600 }}>{s.level}</span>
                    </div>
                    <div style={{ fontWeight:700, fontSize:18, marginBottom:4 }}>{s.name}</div>
                    <div style={{ fontSize:13, color: PALETTE.muted, fontFamily:"sans-serif", marginBottom:12 }}>{s.age} años · {s.phone}</div>
                    <div style={{ display:"flex", gap:12 }}>
                      <div style={{ flex:1, background: PALETTE.softGreen, borderRadius:8, padding:"8px 12px", textAlign:"center" }}>
                        <div style={{ fontSize:18, fontWeight:700, color: PALETTE.forest, fontFamily:"sans-serif" }}>{sc.length}</div>
                        <div style={{ fontSize:10, color: PALETTE.muted, fontFamily:"sans-serif" }}>clases</div>
                      </div>
                      <div style={{ flex:1, background: paidClasses.length === sc.length && sc.length > 0 ? PALETTE.softGreen : PALETTE.lightGold, borderRadius:8, padding:"8px 12px", textAlign:"center" }}>
                        <div style={{ fontSize:14, fontWeight:700, color: PALETTE.terra, fontFamily:"sans-serif" }}>{formatCurrency(s.monthlyFee)}</div>
                        <div style={{ fontSize:10, color: PALETTE.muted, fontFamily:"sans-serif" }}>mensual</div>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Add student modal */}
            {showAddStudent && (
              <div style={{ position:"fixed", inset:0, background:"rgba(0,0,0,0.5)", zIndex:200, display:"flex", alignItems:"center", justifyContent:"center", padding:20 }}>
                <div style={{ background: PALETTE.white, borderRadius:16, padding:32, width:"100%", maxWidth:480, maxHeight:"90vh", overflowY:"auto" }}>
                  <h2 style={{ margin:"0 0 20px", fontSize:22 }}>Nuevo Estudiante</h2>
                  {[["name","Nombre completo","text"],["age","Edad","number"],["phone","Teléfono","text"],["email","Email","email"],["monthlyFee","Mensualidad (COP)","number"]].map(([key,label,type]) => (
                    <div key={key} style={{ marginBottom:16 }}>
                      <label style={{ display:"block", fontFamily:"sans-serif", fontSize:12, fontWeight:600, marginBottom:6, color: PALETTE.muted, textTransform:"uppercase" }}>{label}</label>
                      <input type={type} value={newStudent[key]} onChange={e => setNewStudent(p => ({...p, [key]: e.target.value}))} style={{ width:"100%", padding:"10px 14px", borderRadius:8, border:`1px solid ${PALETTE.lightGold}`, fontFamily:"sans-serif", fontSize:14, boxSizing:"border-box" }} />
                    </div>
                  ))}
                  <div style={{ marginBottom:16 }}>
                    <label style={{ display:"block", fontFamily:"sans-serif", fontSize:12, fontWeight:600, marginBottom:6, color: PALETTE.muted, textTransform:"uppercase" }}>Nivel</label>
                    <select value={newStudent.level} onChange={e => setNewStudent(p => ({...p, level: e.target.value}))} style={{ width:"100%", padding:"10px 14px", borderRadius:8, border:`1px solid ${PALETTE.lightGold}`, fontFamily:"sans-serif", fontSize:14 }}>
                      {Object.keys(LEVEL_GUIDE).map(l => <option key={l} value={l}>{l}</option>)}
                    </select>
                  </div>
                  <div style={{ marginBottom:20 }}>
                    <label style={{ display:"block", fontFamily:"sans-serif", fontSize:12, fontWeight:600, marginBottom:6, color: PALETTE.muted, textTransform:"uppercase" }}>Notas pedagógicas</label>
                    <textarea value={newStudent.notes} onChange={e => setNewStudent(p => ({...p, notes: e.target.value}))} rows={3} style={{ width:"100%", padding:"10px 14px", borderRadius:8, border:`1px solid ${PALETTE.lightGold}`, fontFamily:"sans-serif", fontSize:14, boxSizing:"border-box", resize:"vertical" }} />
                  </div>
                  <div style={{ display:"flex", gap:12 }}>
                    <button onClick={() => setShowAddStudent(false)} style={{ flex:1, padding:"12px", borderRadius:8, border:`1px solid ${PALETTE.lightGold}`, background:"transparent", cursor:"pointer", fontFamily:"sans-serif", fontWeight:600 }}>Cancelar</button>
                    <button onClick={addStudent} style={{ flex:2, padding:"12px", borderRadius:8, border:"none", background: PALETTE.terra, color:"white", cursor:"pointer", fontFamily:"sans-serif", fontWeight:700, fontSize:14 }}>Guardar Estudiante</button>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* STUDENT DETAIL */}
        {activeTab === "students" && selectedStudent && (
          <div>
            <button onClick={() => setSelectedStudent(null)} style={{ marginBottom:20, background:"none", border:"none", cursor:"pointer", color: PALETTE.terra, fontFamily:"sans-serif", fontSize:14, display:"flex", alignItems:"center", gap:6, padding:0 }}>← Volver a Estudiantes</button>
            <div style={{ display:"grid", gridTemplateColumns:"1fr 2fr", gap:20 }}>
              {/* Info */}
              <div>
                <div style={{ background: PALETTE.white, borderRadius:14, padding:24, border:`1px solid ${PALETTE.lightGold}`, marginBottom:16, borderTop:`4px solid ${selectedStudent.color}` }}>
                  <div style={{ width:64, height:64, borderRadius:"50%", background:`linear-gradient(135deg, ${selectedStudent.color}, ${selectedStudent.color}88)`, display:"flex", alignItems:"center", justifyContent:"center", fontSize:28, color:"white", fontWeight:700, fontFamily:"sans-serif", margin:"0 auto 16px" }}>{selectedStudent.name[0]}</div>
                  <h2 style={{ textAlign:"center", margin:"0 0 4px", fontSize:20 }}>{selectedStudent.name}</h2>
                  <p style={{ textAlign:"center", color: PALETTE.muted, fontFamily:"sans-serif", fontSize:13, margin:"0 0 16px" }}>{selectedStudent.age} años</p>
                  <div style={{ background: LEVEL_GUIDE[selectedStudent.level]?.color || PALETTE.gold, color:"white", padding:"8px 16px", borderRadius:20, textAlign:"center", fontFamily:"sans-serif", fontWeight:700, marginBottom:16 }}>{selectedStudent.level}</div>
                  {[["📞", selectedStudent.phone],["📧", selectedStudent.email],["💰", formatCurrency(selectedStudent.monthlyFee) + "/mes"],["📅", `Desde: ${selectedStudent.joinDate}`]].map(([icon, val], i) => (
                    <div key={i} style={{ display:"flex", gap:8, alignItems:"center", padding:"8px 0", borderBottom:`1px solid ${PALETTE.lightGold}`, fontFamily:"sans-serif", fontSize:13 }}>
                      <span>{icon}</span> <span style={{ color: PALETTE.muted }}>{val}</span>
                    </div>
                  ))}
                </div>

                {/* Pedagogical notes */}
                <div style={{ background: PALETTE.lightGold, borderRadius:14, padding:20, border:`1px solid ${PALETTE.gold}` }}>
                  <div style={{ fontSize:12, fontWeight:700, fontFamily:"sans-serif", color: PALETTE.espresso, textTransform:"uppercase", letterSpacing:1, marginBottom:12 }}>📝 Organizador Pedagógico</div>
                  <p style={{ fontFamily:"sans-serif", fontSize:13, color: PALETTE.espresso, margin:"0 0 12px", lineHeight:1.6 }}>{selectedStudent.notes || "Sin notas aún."}</p>
                  <div style={{ background:"white", borderRadius:8, padding:12 }}>
                    <div style={{ fontSize:11, fontWeight:700, fontFamily:"sans-serif", color: PALETTE.muted, marginBottom:8, textTransform:"uppercase" }}>Objetivos actuales según nivel</div>
                    {(LEVEL_GUIDE[selectedStudent.level]?.objectives || []).map((obj, i) => (
                      <div key={i} style={{ display:"flex", gap:8, alignItems:"flex-start", padding:"4px 0", fontFamily:"sans-serif", fontSize:12 }}>
                        <span style={{ color: PALETTE.gold, marginTop:2 }}>◆</span> {obj}
                      </div>
                    ))}
                  </div>
                </div>
              </div>

              {/* Classes history */}
              <div>
                <div style={{ background: PALETTE.white, borderRadius:14, padding:24, border:`1px solid ${PALETTE.lightGold}` }}>
                  <div style={{ display:"flex", justifyContent:"space-between", alignItems:"center", marginBottom:20 }}>
                    <div style={{ fontSize:16, fontWeight:700, fontFamily:"sans-serif" }}>Historial de Clases</div>
                    <button onClick={() => { setNewClass(p=>({...p, studentId: selectedStudent.id})); setShowAddClass(true); }} style={{ background: PALETTE.terra, color:"white", border:"none", padding:"8px 16px", borderRadius:8, cursor:"pointer", fontFamily:"sans-serif", fontSize:13, fontWeight:600 }}>+ Registrar Clase</button>
                  </div>
                  {classes.filter(c => c.studentId === selectedStudent.id).sort((a,b) => new Date(b.date)-new Date(a.date)).map(c => (
                    <div key={c.id} style={{ display:"flex", gap:12, alignItems:"center", padding:"12px 0", borderBottom:`1px solid ${PALETTE.lightGold}` }}>
                      <div style={{ background: c.paid ? PALETTE.softGreen : "#FAE8E0", borderRadius:8, padding:"8px", minWidth:56, textAlign:"center" }}>
                        <div style={{ fontSize:13, fontWeight:700, fontFamily:"sans-serif", color: c.paid ? PALETTE.forest : PALETTE.terra }}>{new Date(c.date+"T12:00").getDate()}</div>
                        <div style={{ fontSize:10, fontFamily:"sans-serif", color: PALETTE.muted }}>{monthNames[new Date(c.date+"T12:00").getMonth()].slice(0,3)}</div>
                      </div>
                      <div style={{ flex:1 }}>
                        <div style={{ fontFamily:"sans-serif", fontSize:14, fontWeight:600 }}>{c.topic}</div>
                        <div style={{ fontFamily:"sans-serif", fontSize:11, color: c.paid ? PALETTE.forest : PALETTE.terra, fontWeight:700, marginTop:2 }}>{c.paid ? "✓ Pagado" : "⏳ Por cobrar"}</div>
                      </div>
                      <button onClick={() => setClasses(prev => prev.map(cl => cl.id === c.id ? {...cl, paid: !cl.paid} : cl))} style={{ background:"none", border:`1px solid ${PALETTE.lightGold}`, borderRadius:6, padding:"6px 10px", cursor:"pointer", fontFamily:"sans-serif", fontSize:11, color: PALETTE.muted }}>{c.paid ? "Marcar pendiente" : "Marcar pagado"}</button>
                    </div>
                  ))}
                  {classes.filter(c => c.studentId === selectedStudent.id).length === 0 && (
                    <div style={{ textAlign:"center", color: PALETTE.muted, fontFamily:"sans-serif", fontSize:13, padding:"32px 0" }}>Sin clases registradas aún.</div>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* ===== CLASSES ===== */}
        {activeTab === "classes" && (
          <div>
            <div style={{ display:"flex", justifyContent:"space-between", alignItems:"center", marginBottom:24 }}>
              <div>
                <h1 style={{ fontSize:28, margin:0, fontWeight:400 }}>Registro de Clases</h1>
                <p style={{ color: PALETTE.muted, fontFamily:"sans-serif", fontSize:13, margin:"4px 0 0" }}>{monthNames[currentMonth]} {currentYear}</p>
              </div>
              <button onClick={() => setShowAddClass(true)} style={{ background: PALETTE.terra, color: PALETTE.white, border:"none", padding:"12px 20px", borderRadius:8, cursor:"pointer", fontFamily:"sans-serif", fontWeight:600, fontSize:14 }}>+ Registrar Clase</button>
            </div>

            {classes.sort((a,b) => new Date(b.date)-new Date(a.date)).map(c => {
              const s = students.find(st => st.id === c.studentId);
              return (
                <div key={c.id} style={{ background: PALETTE.white, borderRadius:12, padding:16, marginBottom:10, border:`1px solid ${PALETTE.lightGold}`, display:"flex", gap:16, alignItems:"center" }}>
                  <div style={{ background: s?.color || PALETTE.gold, borderRadius:10, width:48, height:48, display:"flex", alignItems:"center", justifyContent:"center", fontSize:20, color:"white", fontWeight:700, fontFamily:"sans-serif", flexShrink:0 }}>{s?.name[0] || "?"}</div>
                  <div style={{ flex:1 }}>
                    <div style={{ fontFamily:"sans-serif", fontSize:14, fontWeight:700 }}>{s?.name}</div>
                    <div style={{ fontFamily:"sans-serif", fontSize:13, color: PALETTE.muted, marginTop:2 }}>{c.topic}</div>
                    <div style={{ fontFamily:"sans-serif", fontSize:11, color: PALETTE.muted, marginTop:4 }}>{new Date(c.date+"T12:00").toLocaleDateString("es-CO", {weekday:"long", day:"numeric", month:"long"})}</div>
                  </div>
                  <div style={{ textAlign:"right" }}>
                    <div style={{ fontFamily:"sans-serif", fontSize:13, color: c.paid ? PALETTE.forest : PALETTE.terra, fontWeight:700, marginBottom:6 }}>{c.paid ? "✓ Pagado" : "⏳ Pendiente"}</div>
                    <button onClick={() => setClasses(prev => prev.map(cl => cl.id === c.id ? {...cl, paid: !cl.paid} : cl))} style={{ background:"none", border:`1px solid ${PALETTE.lightGold}`, borderRadius:6, padding:"4px 10px", cursor:"pointer", fontFamily:"sans-serif", fontSize:11 }}>{c.paid ? "Marcar pendiente" : "Marcar pagado"}</button>
                  </div>
                </div>
              );
            })}

            {showAddClass && (
              <div style={{ position:"fixed", inset:0, background:"rgba(0,0,0,0.5)", zIndex:200, display:"flex", alignItems:"center", justifyContent:"center", padding:20 }}>
                <div style={{ background: PALETTE.white, borderRadius:16, padding:32, width:"100%", maxWidth:440 }}>
                  <h2 style={{ margin:"0 0 20px" }}>Registrar Clase</h2>
                  <div style={{ marginBottom:16 }}>
                    <label style={{ display:"block", fontFamily:"sans-serif", fontSize:12, fontWeight:600, marginBottom:6, color: PALETTE.muted, textTransform:"uppercase" }}>Estudiante</label>
                    <select value={newClass.studentId} onChange={e => setNewClass(p=>({...p, studentId: e.target.value}))} style={{ width:"100%", padding:"10px 14px", borderRadius:8, border:`1px solid ${PALETTE.lightGold}`, fontFamily:"sans-serif", fontSize:14 }}>
                      <option value="">Seleccionar...</option>
                      {students.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                    </select>
                  </div>
                  <div style={{ marginBottom:16 }}>
                    <label style={{ display:"block", fontFamily:"sans-serif", fontSize:12, fontWeight:600, marginBottom:6, color: PALETTE.muted, textTransform:"uppercase" }}>Fecha</label>
                    <input type="date" value={newClass.date} onChange={e => setNewClass(p=>({...p, date: e.target.value}))} style={{ width:"100%", padding:"10px 14px", borderRadius:8, border:`1px solid ${PALETTE.lightGold}`, fontFamily:"sans-serif", fontSize:14, boxSizing:"border-box" }} />
                  </div>
                  <div style={{ marginBottom:16 }}>
                    <label style={{ display:"block", fontFamily:"sans-serif", fontSize:12, fontWeight:600, marginBottom:6, color: PALETTE.muted, textTransform:"uppercase" }}>Tema / Contenido</label>
                    <input type="text" value={newClass.topic} onChange={e => setNewClass(p=>({...p, topic: e.target.value}))} placeholder="Ej: Bach Invención No.1, escalas..." style={{ width:"100%", padding:"10px 14px", borderRadius:8, border:`1px solid ${PALETTE.lightGold}`, fontFamily:"sans-serif", fontSize:14, boxSizing:"border-box" }} />
                  </div>
                  <div style={{ marginBottom:20 }}>
                    <label style={{ display:"flex", alignItems:"center", gap:10, cursor:"pointer", fontFamily:"sans-serif", fontSize:14 }}>
                      <input type="checkbox" checked={newClass.paid} onChange={e => setNewClass(p=>({...p, paid: e.target.checked}))} style={{ accentColor: PALETTE.terra, width:18, height:18 }} />
                      Marcar como pagada
                    </label>
                  </div>
                  <div style={{ display:"flex", gap:12 }}>
                    <button onClick={() => setShowAddClass(false)} style={{ flex:1, padding:"12px", borderRadius:8, border:`1px solid ${PALETTE.lightGold}`, background:"transparent", cursor:"pointer", fontFamily:"sans-serif", fontWeight:600 }}>Cancelar</button>
                    <button onClick={addClassEntry} style={{ flex:2, padding:"12px", borderRadius:8, border:"none", background: PALETTE.terra, color:"white", cursor:"pointer", fontFamily:"sans-serif", fontWeight:700, fontSize:14 }}>Guardar</button>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* ===== FINANCES ===== */}
        {activeTab === "finances" && (
          <div>
            <h1 style={{ fontSize:28, margin:"0 0 8px", fontWeight:400 }}>Control Financiero</h1>
            <p style={{ color: PALETTE.muted, fontFamily:"sans-serif", fontSize:13, margin:"0 0 24px" }}>{monthNames[currentMonth]} {currentYear}</p>
            <div style={{ display:"grid", gridTemplateColumns:"repeat(auto-fit, minmax(200px,1fr))", gap:16, marginBottom:24 }}>
              {[
                { label:"Ingreso total esperado", value: formatCurrency(students.reduce((a,s)=>a+s.monthlyFee,0)), bg:`linear-gradient(135deg, ${PALETTE.espresso}, #4A2C1A)`, color:"white" },
                { label:"Cobrado este mes", value: formatCurrency(totalMonthIncome), bg:`linear-gradient(135deg, ${PALETTE.forest}, #4A8020)`, color:"white" },
                { label:"Por cobrar", value: formatCurrency(pendingAmount), bg:`linear-gradient(135deg, #C0392B, #E74C3C)`, color:"white" },
              ].map((s,i) => (
                <div key={i} style={{ background: s.bg, borderRadius:14, padding:24, color: s.color }}>
                  <div style={{ fontSize:28, fontWeight:700, fontFamily:"sans-serif" }}>{s.value}</div>
                  <div style={{ fontSize:12, opacity:0.8, fontFamily:"sans-serif", marginTop:6 }}>{s.label}</div>
                </div>
              ))}
            </div>

            <div style={{ background: PALETTE.white, borderRadius:14, padding:24, border:`1px solid ${PALETTE.lightGold}` }}>
              <div style={{ fontSize:16, fontWeight:700, fontFamily:"sans-serif", marginBottom:20, textTransform:"uppercase", letterSpacing:1 }}>Detalle por Estudiante — {monthNames[currentMonth]}</div>
              {students.map(s => {
                const sc = monthClasses.filter(c => c.studentId === s.id);
                const paid = sc.some(c => c.paid);
                const hasPending = sc.some(c => !c.paid);
                return (
                  <div key={s.id} style={{ display:"flex", gap:16, alignItems:"center", padding:"14px 0", borderBottom:`1px solid ${PALETTE.lightGold}` }}>
                    <div style={{ width:40, height:40, borderRadius:"50%", background: s.color, display:"flex", alignItems:"center", justifyContent:"center", fontSize:16, color:"white", fontWeight:700, fontFamily:"sans-serif", flexShrink:0 }}>{s.name[0]}</div>
                    <div style={{ flex:1 }}>
                      <div style={{ fontFamily:"sans-serif", fontSize:14, fontWeight:700 }}>{s.name}</div>
                      <div style={{ fontFamily:"sans-serif", fontSize:12, color: PALETTE.muted }}>{sc.length} clases registradas este mes</div>
                    </div>
                    <div style={{ fontFamily:"sans-serif", fontSize:15, fontWeight:700, color: PALETTE.espresso }}>{formatCurrency(s.monthlyFee)}</div>
                    <div style={{ width:100, textAlign:"right" }}>
                      {sc.length === 0 ? (
                        <span style={{ fontSize:11, color: PALETTE.muted, fontFamily:"sans-serif" }}>Sin clases</span>
                      ) : paid && !hasPending ? (
                        <span style={{ background: PALETTE.softGreen, color: PALETTE.forest, padding:"4px 12px", borderRadius:12, fontSize:11, fontFamily:"sans-serif", fontWeight:700 }}>✓ Pagado</span>
                      ) : (
                        <span style={{ background:"#FDECEA", color:"#C0392B", padding:"4px 12px", borderRadius:12, fontSize:11, fontFamily:"sans-serif", fontWeight:700 }}>⏳ Pendiente</span>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* ===== TASKS ===== */}
        {activeTab === "tasks" && (
          <div>
            <div style={{ display:"flex", justifyContent:"space-between", alignItems:"center", marginBottom:24 }}>
              <h1 style={{ fontSize:28, margin:0, fontWeight:400 }}>Lista de Tareas</h1>
              <button onClick={() => setShowAddTask(true)} style={{ background: PALETTE.terra, color:"white", border:"none", padding:"12px 20px", borderRadius:8, cursor:"pointer", fontFamily:"sans-serif", fontWeight:600 }}>+ Nueva Tarea</button>
            </div>
            {["alta","media","baja"].map(priority => {
              const tList = tasks.filter(t => t.priority === priority);
              if (tList.length === 0) return null;
              const colors = { alta:"#C0392B", media: PALETTE.terra, baja: PALETTE.muted };
              const bgs = { alta:"#FDECEA", media:"#FEF5E7", baja: PALETTE.lightGold };
              return (
                <div key={priority} style={{ marginBottom:20 }}>
                  <div style={{ fontSize:11, fontWeight:700, fontFamily:"sans-serif", textTransform:"uppercase", letterSpacing:2, color: colors[priority], marginBottom:10 }}>Prioridad {priority}</div>
                  {tList.map(t => (
                    <div key={t.id} style={{ background: t.done ? PALETTE.lightGold : PALETTE.white, borderRadius:10, padding:"14px 16px", marginBottom:8, border:`1px solid ${PALETTE.lightGold}`, display:"flex", gap:12, alignItems:"center", opacity: t.done ? 0.6 : 1 }}>
                      <input type="checkbox" checked={t.done} onChange={() => toggleTask(t.id)} style={{ accentColor: PALETTE.terra, width:18, height:18, flexShrink:0 }} />
                      <span style={{ flex:1, fontFamily:"sans-serif", fontSize:14, textDecoration: t.done ? "line-through" : "none", color: t.done ? PALETTE.muted : PALETTE.espresso }}>{t.text}</span>
                      <button onClick={() => deleteTask(t.id)} style={{ background:"none", border:"none", cursor:"pointer", color: PALETTE.muted, fontSize:16 }}>×</button>
                    </div>
                  ))}
                </div>
              );
            })}

            {showAddTask && (
              <div style={{ position:"fixed", inset:0, background:"rgba(0,0,0,0.5)", zIndex:200, display:"flex", alignItems:"center", justifyContent:"center", padding:20 }}>
                <div style={{ background: PALETTE.white, borderRadius:16, padding:32, width:"100%", maxWidth:400 }}>
                  <h2 style={{ margin:"0 0 20px" }}>Nueva Tarea</h2>
                  <div style={{ marginBottom:16 }}>
                    <input type="text" value={newTask.text} onChange={e => setNewTask(p=>({...p, text: e.target.value}))} placeholder="Descripción de la tarea..." style={{ width:"100%", padding:"12px 14px", borderRadius:8, border:`1px solid ${PALETTE.lightGold}`, fontFamily:"sans-serif", fontSize:14, boxSizing:"border-box" }} />
                  </div>
                  <div style={{ marginBottom:20 }}>
                    <label style={{ display:"block", fontFamily:"sans-serif", fontSize:12, fontWeight:600, marginBottom:6, color: PALETTE.muted, textTransform:"uppercase" }}>Prioridad</label>
                    <div style={{ display:"flex", gap:8 }}>
                      {["alta","media","baja"].map(p => (
                        <button key={p} onClick={() => setNewTask(prev=>({...prev, priority:p}))} style={{ flex:1, padding:"10px", borderRadius:8, border:`2px solid ${newTask.priority === p ? PALETTE.terra : PALETTE.lightGold}`, background: newTask.priority === p ? PALETTE.terra : "transparent", color: newTask.priority === p ? "white" : PALETTE.espresso, cursor:"pointer", fontFamily:"sans-serif", fontWeight:600, fontSize:13 }}>{p}</button>
                      ))}
                    </div>
                  </div>
                  <div style={{ display:"flex", gap:12 }}>
                    <button onClick={() => setShowAddTask(false)} style={{ flex:1, padding:"12px", borderRadius:8, border:`1px solid ${PALETTE.lightGold}`, background:"transparent", cursor:"pointer", fontFamily:"sans-serif", fontWeight:600 }}>Cancelar</button>
                    <button onClick={addTask} style={{ flex:2, padding:"12px", borderRadius:8, border:"none", background: PALETTE.terra, color:"white", cursor:"pointer", fontFamily:"sans-serif", fontWeight:700 }}>Guardar</button>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* ===== CALENDAR ===== */}
        {activeTab === "calendar" && (
          <div>
            <div style={{ display:"flex", justifyContent:"space-between", alignItems:"center", marginBottom:24 }}>
              <h1 style={{ fontSize:28, margin:0, fontWeight:400 }}>Agenda & Eventos</h1>
              <button onClick={() => setShowAddEvent(true)} style={{ background: PALETTE.terra, color:"white", border:"none", padding:"12px 20px", borderRadius:8, cursor:"pointer", fontFamily:"sans-serif", fontWeight:600 }}>+ Nuevo Evento</button>
            </div>
            <div style={{ display:"grid", gridTemplateColumns:"repeat(auto-fill, minmax(280px,1fr))", gap:16 }}>
              {events.sort((a,b) => new Date(a.date)-new Date(b.date)).map(ev => {
                const typeColors = { recital: PALETTE.terra, masterclass: PALETTE.forest, evaluacion: "#7B68EE", otro: PALETTE.muted };
                const typeLabels = { recital:"🎹 Recital", masterclass:"🎓 Masterclass", evaluacion:"📊 Evaluación", otro:"📌 Otro" };
                const d = new Date(ev.date + "T12:00:00");
                const isPast = d < today;
                return (
                  <div key={ev.id} style={{ background: PALETTE.white, borderRadius:14, overflow:"hidden", border:`1px solid ${PALETTE.lightGold}`, opacity: isPast ? 0.65 : 1 }}>
                    <div style={{ background: typeColors[ev.type] || PALETTE.gold, padding:"20px 24px", color:"white" }}>
                      <div style={{ fontSize:11, fontFamily:"sans-serif", fontWeight:700, textTransform:"uppercase", letterSpacing:1, marginBottom:8, opacity:0.9 }}>{typeLabels[ev.type]}</div>
                      <div style={{ fontSize:32, fontWeight:700, fontFamily:"sans-serif" }}>{d.getDate()}</div>
                      <div style={{ fontSize:13, opacity:0.8, fontFamily:"sans-serif" }}>{monthNames[d.getMonth()]} {d.getFullYear()}</div>
                    </div>
                    <div style={{ padding:"16px 24px" }}>
                      <div style={{ fontWeight:700, fontSize:16, marginBottom:6 }}>{ev.title}</div>
                      <div style={{ fontFamily:"sans-serif", fontSize:13, color: PALETTE.muted }}>{ev.description}</div>
                      {isPast && <div style={{ marginTop:10, fontSize:11, fontFamily:"sans-serif", color: PALETTE.muted, fontStyle:"italic" }}>Evento pasado</div>}
                    </div>
                  </div>
                );
              })}
            </div>
            {showAddEvent && (
              <div style={{ position:"fixed", inset:0, background:"rgba(0,0,0,0.5)", zIndex:200, display:"flex", alignItems:"center", justifyContent:"center", padding:20 }}>
                <div style={{ background: PALETTE.white, borderRadius:16, padding:32, width:"100%", maxWidth:440 }}>
                  <h2 style={{ margin:"0 0 20px" }}>Nuevo Evento</h2>
                  {[["title","Título","text"],["date","Fecha","date"],["description","Descripción","text"]].map(([key,label,type]) => (
                    <div key={key} style={{ marginBottom:16 }}>
                      <label style={{ display:"block", fontFamily:"sans-serif", fontSize:12, fontWeight:600, marginBottom:6, color: PALETTE.muted, textTransform:"uppercase" }}>{label}</label>
                      <input type={type} value={newEvent[key]} onChange={e => setNewEvent(p=>({...p, [key]: e.target.value}))} style={{ width:"100%", padding:"10px 14px", borderRadius:8, border:`1px solid ${PALETTE.lightGold}`, fontFamily:"sans-serif", fontSize:14, boxSizing:"border-box" }} />
                    </div>
                  ))}
                  <div style={{ marginBottom:20 }}>
                    <label style={{ display:"block", fontFamily:"sans-serif", fontSize:12, fontWeight:600, marginBottom:6, color: PALETTE.muted, textTransform:"uppercase" }}>Tipo</label>
                    <select value={newEvent.type} onChange={e => setNewEvent(p=>({...p, type: e.target.value}))} style={{ width:"100%", padding:"10px 14px", borderRadius:8, border:`1px solid ${PALETTE.lightGold}`, fontFamily:"sans-serif", fontSize:14 }}>
                      <option value="recital">Recital</option>
                      <option value="masterclass">Masterclass</option>
                      <option value="evaluacion">Evaluación</option>
                      <option value="otro">Otro</option>
                    </select>
                  </div>
                  <div style={{ display:"flex", gap:12 }}>
                    <button onClick={() => setShowAddEvent(false)} style={{ flex:1, padding:"12px", borderRadius:8, border:`1px solid ${PALETTE.lightGold}`, background:"transparent", cursor:"pointer", fontFamily:"sans-serif", fontWeight:600 }}>Cancelar</button>
                    <button onClick={addEvent} style={{ flex:2, padding:"12px", borderRadius:8, border:"none", background: PALETTE.terra, color:"white", cursor:"pointer", fontFamily:"sans-serif", fontWeight:700 }}>Guardar</button>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* ===== METRONOME ===== */}
        {activeTab === "metronome" && <MetronomeSection />}

        {/* ===== GUIDE ===== */}
        {activeTab === "guide" && (
          <div>
            <h1 style={{ fontSize:28, margin:"0 0 8px", fontWeight:400 }}>Guía de Niveles</h1>
            <p style={{ color: PALETTE.muted, fontFamily:"sans-serif", fontSize:13, margin:"0 0 24px" }}>Referencia pedagógica por nivel de aprendizaje</p>
            <div style={{ display:"grid", gridTemplateColumns:"repeat(auto-fill, minmax(280px,1fr))", gap:20 }}>
              {Object.entries(LEVEL_GUIDE).map(([level, info]) => (
                <div key={level} style={{ background: PALETTE.white, borderRadius:16, overflow:"hidden", border:`1px solid ${PALETTE.lightGold}` }}>
                  <div style={{ background: info.color, padding:"20px 24px", color:"white" }}>
                    <div style={{ fontSize:22, fontWeight:700, fontFamily:"sans-serif" }}>{level}</div>
                    <div style={{ fontSize:12, opacity:0.9, fontFamily:"sans-serif", marginTop:4 }}>Duración aproximada: {info.duration}</div>
                  </div>
                  <div style={{ padding:20 }}>
                    <div style={{ fontSize:12, fontWeight:700, fontFamily:"sans-serif", textTransform:"uppercase", letterSpacing:1, color: PALETTE.muted, marginBottom:10 }}>Objetivos</div>
                    {info.objectives.map((obj, i) => (
                      <div key={i} style={{ display:"flex", gap:8, alignItems:"flex-start", padding:"4px 0", fontFamily:"sans-serif", fontSize:13 }}>
                        <span style={{ color: info.color, fontWeight:700, flexShrink:0 }}>→</span> {obj}
                      </div>
                    ))}
                    <div style={{ fontSize:12, fontWeight:700, fontFamily:"sans-serif", textTransform:"uppercase", letterSpacing:1, color: PALETTE.muted, marginTop:16, marginBottom:10 }}>Repertorio Sugerido</div>
                    {info.repertoire.map((r, i) => (
                      <div key={i} style={{ fontFamily:"sans-serif", fontSize:12, padding:"4px 0", color: PALETTE.espresso, borderBottom:`1px solid ${PALETTE.lightGold}` }}>♩ {r}</div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </main>

      <style>{`
        @keyframes slideIn { from { transform: translateX(100px); opacity:0; } to { transform: translateX(0); opacity:1; } }
        * { box-sizing: border-box; }
        ::-webkit-scrollbar { width: 6px; height: 6px; }
        ::-webkit-scrollbar-track { background: #F5F0E8; }
        ::-webkit-scrollbar-thumb { background: #C9A84C; border-radius: 3px; }
      `}</style>
    </div>
  );
}

// ============================================================
// METRONOME COMPONENT
// ============================================================
function MetronomeSection() {
  const [bpm, setBpm] = useState(80);
  const [isPlaying, setIsPlaying] = useState(false);
  const [beat, setBeat] = useState(0);
  const [timeSignature, setTimeSignature] = useState(4);
  const [accent, setAccent] = useState(true);
  const intervalRef = useRef(null);
  const audioCtxRef = useRef(null);

  const playClick = useCallback((isAccent) => {
    try {
      if (!audioCtxRef.current) audioCtxRef.current = new (window.AudioContext || window.webkitAudioContext)();
      const ctx = audioCtxRef.current;
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.frequency.value = isAccent ? 1200 : 800;
      gain.gain.setValueAtTime(isAccent ? 0.4 : 0.25, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.08);
      osc.start(ctx.currentTime);
      osc.stop(ctx.currentTime + 0.08);
    } catch(e) {}
  }, []);

  useEffect(() => {
    if (isPlaying) {
      let currentBeat = 0;
      intervalRef.current = setInterval(() => {
        setBeat(b => {
          const next = (b + 1) % timeSignature;
          playClick(accent && next === 0);
          return next;
        });
      }, (60 / bpm) * 1000);
    } else {
      clearInterval(intervalRef.current);
      setBeat(0);
    }
    return () => clearInterval(intervalRef.current);
  }, [isPlaying, bpm, timeSignature, accent, playClick]);

  const commonTempos = [
    { name:"Largo", range:"40-60" }, { name:"Adagio", range:"60-76" }, { name:"Andante", range:"76-108" },
    { name:"Moderato", range:"108-120" }, { name:"Allegro", range:"120-156" }, { name:"Presto", range:"168-200" }
  ];

  return (
    <div>
      <h1 style={{ fontSize:28, margin:"0 0 8px", fontWeight:400 }}>Metrónomo</h1>
      <p style={{ color: PALETTE.muted, fontFamily:"sans-serif", fontSize:13, margin:"0 0 32px" }}>Herramienta de práctica rítmica</p>

      <div style={{ maxWidth:500, margin:"0 auto" }}>
        <div style={{ background: PALETTE.espresso, borderRadius:20, padding:40, textAlign:"center", marginBottom:24, color:"white", position:"relative" }}>
          {/* Pendulum visual */}
          <div style={{ height:80, display:"flex", alignItems:"center", justifyContent:"center", marginBottom:32 }}>
            <div style={{ display:"flex", gap:12, alignItems:"flex-end" }}>
              {Array.from({length: timeSignature}).map((_, i) => (
                <div key={i} style={{ width:24, height: i === beat && isPlaying ? 72 : 48, background: i === beat && isPlaying ? (accent && i === 0 ? PALETTE.gold : PALETTE.terra) : "rgba(255,255,255,0.2)", borderRadius:4, transition:"height 0.08s ease, background 0.08s ease" }} />
              ))}
            </div>
          </div>

          {/* BPM display */}
          <div style={{ fontSize:80, fontWeight:700, fontFamily:"sans-serif", lineHeight:1, marginBottom:8 }}>{bpm}</div>
          <div style={{ fontSize:14, opacity:0.6, fontFamily:"sans-serif", marginBottom:32 }}>BPM</div>

          {/* BPM slider */}
          <input type="range" min={20} max={240} value={bpm} onChange={e => setBpm(Number(e.target.value))} style={{ width:"100%", marginBottom:32, accentColor: PALETTE.gold, height:6 }} />

          {/* Controls */}
          <div style={{ display:"flex", gap:12, justifyContent:"center", alignItems:"center", marginBottom:24 }}>
            <button onClick={() => setBpm(b => Math.max(20, b-5))} style={{ background:"rgba(255,255,255,0.1)", border:"none", color:"white", width:44, height:44, borderRadius:10, cursor:"pointer", fontSize:20, fontFamily:"sans-serif" }}>−</button>
            <button onClick={() => setIsPlaying(p => !p)} style={{ background: isPlaying ? PALETTE.terra : PALETTE.gold, border:"none", color: PALETTE.espresso, width:80, height:80, borderRadius:"50%", cursor:"pointer", fontSize:24, fontFamily:"sans-serif", fontWeight:700, transition:"transform 0.1s", transform: isPlaying ? "scale(0.95)" : "scale(1)" }}>
              {isPlaying ? "⏹" : "▶"}
            </button>
            <button onClick={() => setBpm(b => Math.min(240, b+5))} style={{ background:"rgba(255,255,255,0.1)", border:"none", color:"white", width:44, height:44, borderRadius:10, cursor:"pointer", fontSize:20, fontFamily:"sans-serif" }}>+</button>
          </div>

          {/* Time signature */}
          <div style={{ display:"flex", gap:8, justifyContent:"center", marginBottom:16 }}>
            {[2,3,4,6].map(sig => (
              <button key={sig} onClick={() => { setTimeSignature(sig); setBeat(0); }} style={{ background: timeSignature === sig ? PALETTE.gold : "rgba(255,255,255,0.1)", border:"none", color: timeSignature === sig ? PALETTE.espresso : "white", padding:"8px 16px", borderRadius:8, cursor:"pointer", fontFamily:"sans-serif", fontWeight:700, fontSize:14 }}>{sig}/4</button>
            ))}
          </div>
          <label style={{ display:"flex", alignItems:"center", justifyContent:"center", gap:8, cursor:"pointer", fontFamily:"sans-serif", fontSize:13, color:"rgba(255,255,255,0.7)" }}>
            <input type="checkbox" checked={accent} onChange={e => setAccent(e.target.checked)} style={{ accentColor: PALETTE.gold }} />
            Acentuar primer tiempo
          </label>
        </div>

        {/* Tempo reference */}
        <div style={{ background: PALETTE.white, borderRadius:14, padding:20, border:`1px solid ${PALETTE.lightGold}` }}>
          <div style={{ fontSize:13, fontWeight:700, fontFamily:"sans-serif", textTransform:"uppercase", letterSpacing:1, marginBottom:14, color: PALETTE.espresso }}>Referencia de Tempos</div>
          <div style={{ display:"grid", gridTemplateColumns:"repeat(3, 1fr)", gap:8 }}>
            {commonTempos.map(t => (
              <button key={t.name} onClick={() => setBpm(Number(t.range.split("-")[0]) + Math.floor((Number(t.range.split("-")[1]) - Number(t.range.split("-")[0])) / 2))} style={{ background: PALETTE.lightGold, border:"none", borderRadius:10, padding:"12px 8px", cursor:"pointer", textAlign:"center" }}>
                <div style={{ fontWeight:700, fontSize:13, fontFamily:"sans-serif", color: PALETTE.espresso }}>{t.name}</div>
                <div style={{ fontSize:11, color: PALETTE.muted, fontFamily:"sans-serif", marginTop:2 }}>{t.range}</div>
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
