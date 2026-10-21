// Recorre el módulo en el navegador (Chrome sin ventana, protocolo DevTools) y captura cada pantalla.
// Uso: node pruebas/capturar_pantallas.js   (con la aplicación desplegada en Tomcat)
const { spawn } = require("child_process"); const fs = require("fs"); const os = require("os"); const path = require("path");
const BASE = "http://localhost:8080/medicitas-especialidades/especialidades";
const SALIDA = path.join(__dirname, "capturas");
const esperar = (ms) => new Promise((r) => setTimeout(r, ms));
(async () => {
  const chrome = spawn("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome", ["--headless=new", "--remote-debugging-port=9339", `--user-data-dir=${fs.mkdtempSync(path.join(os.tmpdir(), "s-"))}`, "about:blank"], { stdio: "ignore" });
  try {
    let l; for (let i = 0; i < 40 && !l; i++) { await esperar(300); l = await fetch("http://127.0.0.1:9339/json/list").then(r => r.json()).catch(() => null); }
    const ws = new WebSocket(l.find(p => p.type === "page").webSocketDebuggerUrl); await new Promise(o => ws.onopen = o);
    let id = 0; const pend = new Map(); ws.onmessage = e => { const m = JSON.parse(e.data); if (pend.has(m.id)) { pend.get(m.id)(m.result); pend.delete(m.id); } };
    const cdp = (method, params = {}) => new Promise(o => { pend.set(++id, o); ws.send(JSON.stringify({ id, method, params })); });
    const js = async (e) => (await cdp("Runtime.evaluate", { expression: e, returnByValue: true })).result?.value;
    const ir = async (url) => { await cdp("Page.navigate", { url }); await esperar(1200); };
    const enviar = async (codigo) => { await js(codigo); await esperar(1500); };
    const captura = async (nombre) => {
      const alto = Math.max(700, await js("document.documentElement.scrollHeight"));
      await cdp("Emulation.setDeviceMetricsOverride", { width: 1100, height: alto, deviceScaleFactor: 1, mobile: false });
      const img = await cdp("Page.captureScreenshot", { format: "png" });
      fs.writeFileSync(path.join(SALIDA, nombre + ".png"), Buffer.from(img.data, "base64"));
      console.log("captura", nombre, "·", await js("location.pathname + location.search"));
    };
    await cdp("Page.enable");
    await cdp("Emulation.setDeviceMetricsOverride", { width: 1100, height: 700, deviceScaleFactor: 1, mobile: false });
    await ir(BASE); await captura("01_listado_get");
    await ir(BASE + "?buscar=med"); await captura("02_busqueda_get");
    await ir(BASE + "?accion=nuevo");
    await js(`document.getElementById('nombre').value = 'Neurología'; document.getElementById('descripcion').value = 'Enfermedades del cerebro y del sistema nervioso';`);
    await captura("03_formulario_nuevo");
    await enviar("document.querySelector('form.formulario').submit()"); await captura("04_creada_post");
    await ir(BASE + "?accion=nuevo");
    await js(`document.getElementById('nombre').value = 'Neurología';`);
    await enviar("document.querySelector('form.formulario').submit()"); await captura("05_error_nombre_repetido");
    const idN = await (async () => { await ir(BASE + "?buscar=Neurolog"); return js(`(document.body.innerHTML.match(/accion=editar&amp;id=(\\d+)/) || [])[1]`); })();
    await ir(BASE + "?accion=editar&id=" + idN);
    await js(`document.getElementById('descripcion').value = 'Cerebro, médula espinal y nervios';`);
    await captura("06_formulario_editar");
    await enviar("document.querySelector('form.formulario').submit()"); await captura("07_actualizada_post");
    await ir(BASE);
    await enviar(`window.confirm = () => true; [...document.querySelectorAll('tbody tr')].find(f => f.innerText.includes('Medicina General')).querySelector('form button').click()`);
    await captura("08_error_eliminar_con_medicos");
    await enviar(`window.confirm = () => true; [...document.querySelectorAll('tbody tr')].find(f => f.innerText.includes('Neurología')).querySelector('form button').click()`);
    await captura("09_eliminada_post");
    console.log("filas finales:", await js("document.querySelectorAll('tbody tr').length"));
    ws.close();
  } finally { chrome.kill(); }
})().catch(e => { console.error(e); process.exit(1); });
