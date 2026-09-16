/**
 * ============================================================================
 * Laboratorul 1 - Paradigma Scriptică (JavaScript Nativ)
 * Implementarea celor 20 de sarcini (Sarcina 1 + Sarcina 2)
 * ============================================================================
 */

document.addEventListener("DOMContentLoaded", () => {
  // Inițializare temă din localStorage (Sarcina 2.6 & 2.7)
  initTheme();

  // Inițializare toate sarcinile
  initTask1_1();
  initTask1_2();
  initTask1_3();
  initTask1_4();
  initTask1_5();
  initTask1_6();
  initTask1_7();
  initTask1_8();
  initTask1_9();
  initTask1_10();

  initTask2_1();
  initTask2_2();
  initTask2_3_4();
  initTask2_5();
  initTask2_6_7();
  initTask2_8();
  initTask2_9();
  initTask2_10();
});

/* ============================================================================
   SARCINA 1: CERINȚE DE BAZĂ (Nota 5)
   ============================================================================ */

/**
 * 1.1: Citește două numere de la utilizator folosind prompt() și afișează suma lor.
 */
function initTask1_1() {
  const btn = document.getElementById("btn-prompt-sum");
  const res = document.getElementById("res-prompt-sum");

  if (!btn || !res) return;

  btn.addEventListener("click", () => {
    const input1 = prompt("Sarcina 1.1: Introduceți primul număr:");
    if (input1 === null) {
      res.innerHTML = "❌ Operațiune anulată de utilizator la primul număr.";
      return;
    }

    const input2 = prompt("Sarcina 1.1: Introduceți al doilea număr:");
    if (input2 === null) {
      res.innerHTML = "❌ Operațiune anulată de utilizator la al doilea număr.";
      return;
    }

    const num1 = parseFloat(input1.trim());
    const num2 = parseFloat(input2.trim());

    if (isNaN(num1) || isNaN(num2)) {
      res.innerHTML = `⚠️ <strong>Eroare:</strong> Cel puțin una dintre valorile introduse nu este un număr valid (Ai introdus: "${input1}" și "${input2}").`;
      return;
    }

    const suma = num1 + num2;
    res.innerHTML = `✅ <strong>Suma calculată:</strong> ${num1} + ${num2} = <strong style="color: var(--accent-color);">${suma}</strong>`;
  });
}

/**
 * 1.2: Calculează factorialul unui număr introdus de utilizator.
 */
function initTask1_2() {
  const btn = document.getElementById("btn-factorial");
  const input = document.getElementById("factorial-input");
  const res = document.getElementById("res-factorial");

  if (!btn || !input || !res) return;

  function compute() {
    const rawVal = input.value.trim();
    if (rawVal === "") {
      res.innerHTML = "⚠️ Vă rugăm introduceți un număr.";
      return;
    }

    const n = Number(rawVal);
    if (!Number.isInteger(n) || n < 0) {
      res.innerHTML = "⚠️ Factorialul (n!) este definit doar pentru numere întregi non-negative (n ≥ 0).";
      return;
    }

    if (n > 1000) {
      res.innerHTML = "⚠️ Vă rugăm introduceți un număr ≤ 1000 pentru a preveni supraîncărcarea memoriei.";
      return;
    }

    // Calcul factorial folosind BigInt pentru precizie absolută
    let factorial = 1n;
    for (let i = 2n; i <= BigInt(n); i++) {
      factorial *= i;
    }

    let factString = factorial.toString();
    if (factString.length > 30) {
      factString = factString.slice(0, 25) + `... (${factString.length} cifre)`;
    }

    res.innerHTML = `✅ <strong>${n}!</strong> = <strong style="color: var(--accent-color);">${factString}</strong>`;
  }

  btn.addEventListener("click", compute);
  input.addEventListener("keydown", (e) => {
    if (e.key === "Enter") compute();
  });
}

/**
 * 1.3: Numără câte vocale conține un text introdus de la tastatură.
 */
function initTask1_3() {
  const btn = document.getElementById("btn-count-vowels");
  const input = document.getElementById("vowels-input");
  const res = document.getElementById("res-vowels");

  if (!btn || !input || !res) return;

  function countVowels() {
    const text = input.value;
    // Set de vocale (inclusiv diacritice românești)
    const vowelsRegex = /[aeiouăâîAEIOUĂÂÎ]/g;
    const matches = text.match(vowelsRegex) || [];
    const count = matches.length;

    // Distribuția vocalelor
    const freq = {};
    matches.forEach(ch => {
      const lower = ch.toLowerCase();
      freq[lower] = (freq[lower] || 0) + 1;
    });

    const breakdown = Object.entries(freq)
      .map(([v, c]) => `<code>${v}</code>: ${c}`)
      .join(", ");

    res.innerHTML = `✅ <strong>Total vocale găsite:</strong> <strong style="color: var(--accent-color);">${count}</strong>` +
      (count > 0 ? `<br><small style="color: var(--text-secondary);">Distribuție: ${breakdown}</small>` : "");
  }

  btn.addEventListener("click", countVowels);
  input.addEventListener("input", countVowels);
  // Calcul inițial la încărcare
  countVowels();
}

/**
 * 1.4: Creează un script pentru un buton care schimbă culoarea fundalului paginii la o culoare aleatoare.
 */
function initTask1_4() {
  const btnRandom = document.getElementById("btn-random-bg");
  const btnReset = document.getElementById("btn-reset-bg");
  const res = document.getElementById("res-bg-color");

  if (!btnRandom || !btnReset || !res) return;

  btnRandom.addEventListener("click", () => {
    // Generare culoare RGB aleatoare
    const r = Math.floor(Math.random() * 256);
    const g = Math.floor(Math.random() * 256);
    const b = Math.floor(Math.random() * 256);
    const hex = `#${r.toString(16).padStart(2, "0")}${g.toString(16).padStart(2, "0")}${b.toString(16).padStart(2, "0")}`;

    document.body.style.backgroundColor = hex;
    res.innerHTML = `🎨 Culoare fundal aplicată: <strong>${hex.toUpperCase()}</strong> (RGB: ${r}, ${g}, ${b})`;
  });

  btnReset.addEventListener("click", () => {
    document.body.style.backgroundColor = "";
    res.innerHTML = "↺ Culoare fundal resetată la valorile implicite ale temei.";
  });
}

/**
 * 1.5: Creează un cronometru care pornește automat și afișează secundele trecute într-un element HTML.
 */
function initTask1_5() {
  const display = document.getElementById("auto-timer-display");
  const countEl = document.getElementById("auto-timer-count");

  if (!display || !countEl) return;

  let seconds = 0;
  setInterval(() => {
    seconds++;
    display.textContent = `${seconds} secunde`;
    countEl.textContent = seconds;
  }, 1000);
}

/**
 * 1.6: Creează o listă <ul> și un buton care, la clic, adaugă un element nou în listă cu textul introdus de utilizator.
 */
function initTask1_6() {
  const btn = document.getElementById("btn-add-to-list");
  const input = document.getElementById("dynamic-list-input");
  const ul = document.getElementById("dynamic-ul");

  if (!btn || !input || !ul) return;

  function addItem() {
    const text = input.value.trim();
    if (text === "") {
      alert("Introduceți un text înainte de a adăuga în listă!");
      return;
    }

    const li = document.createElement("li");
    li.textContent = text;
    ul.appendChild(li);

    input.value = "";
    input.focus();
  }

  btn.addEventListener("click", addItem);
  input.addEventListener("keydown", (e) => {
    if (e.key === "Enter") addItem();
  });
}

/**
 * 1.7: Scrie un script care ascunde/afișează un paragraf atunci când se apasă un buton.
 */
function initTask1_7() {
  const btn = document.getElementById("btn-toggle-paragraph");
  const para = document.getElementById("target-paragraph");

  if (!btn || !para) return;

  btn.addEventListener("click", () => {
    if (para.style.display === "none") {
      para.style.display = "block";
      btn.textContent = "Ascunde paragraful";
    } else {
      para.style.display = "none";
      btn.textContent = "Afișează paragraful";
    }
  });
}

/**
 * 1.8: La trecerea mouse-ului peste un element, schimbă culoarea textului; la ieșire, revine la culoarea inițială.
 */
function initTask1_8() {
  const box = document.getElementById("hover-target-box");
  const status = document.getElementById("hover-status");

  if (!box || !status) return;

  const originalColor = window.getComputedStyle(box).color;

  box.addEventListener("mouseenter", () => {
    box.style.color = "#ef4444"; // Culoare roșie evidențiată
    box.style.borderColor = "#ef4444";
    status.innerHTML = "Stare: <strong style='color: #ef4444;'>Cursorul este deasupra (culoare schimbată)</strong>";
  });

  box.addEventListener("mouseleave", () => {
    box.style.color = originalColor;
    box.style.borderColor = "";
    status.innerHTML = "Stare: În afara elementului (revenire la culoarea inițială)";
  });
}

/**
 * 1.9: Creează un slider de imagini simplu care schimbă poza la fiecare 3 secunde.
 */
function initTask1_9() {
  const imgEl = document.getElementById("slider-image");
  const captionEl = document.getElementById("slider-caption");
  const dotsContainer = document.getElementById("slider-dots");
  const btnPrev = document.getElementById("btn-slider-prev");
  const btnNext = document.getElementById("btn-slider-next");

  if (!imgEl || !captionEl || !dotsContainer || !btnPrev || !btnNext) return;

  // Imagini SVG complet autonome (nu depind de rețea, 100% funcționale offline)
  const slides = [
    {
      title: "Slide 1: JavaScript Nativ",
      svg: `data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="600" height="300" viewBox="0 0 600 300"><defs><linearGradient id="g1" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stop-color="%23f59e0b"/><stop offset="100%" stop-color="%23d97706"/></linearGradient></defs><rect width="100%" height="100%" fill="url(%23g1)"/><text x="50%" y="45%" dominant-baseline="middle" text-anchor="middle" font-family="sans-serif" font-size="28" font-weight="bold" fill="white">1. JavaScript Nativ</text><text x="50%" y="62%" dominant-baseline="middle" text-anchor="middle" font-family="sans-serif" font-size="16" fill="%23fef3c7">Manipularea DOM-ului &amp; Evenimente</text></svg>`
    },
    {
      title: "Slide 2: Funcții Asincrone",
      svg: `data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="600" height="300" viewBox="0 0 600 300"><defs><linearGradient id="g2" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stop-color="%233b82f6"/><stop offset="100%" stop-color="%231d4ed8"/></linearGradient></defs><rect width="100%" height="100%" fill="url(%23g2)"/><text x="50%" y="45%" dominant-baseline="middle" text-anchor="middle" font-family="sans-serif" font-size="28" font-weight="bold" fill="white">2. Programare Asincronă</text><text x="50%" y="62%" dominant-baseline="middle" text-anchor="middle" font-family="sans-serif" font-size="16" fill="%23dbeafe">async / await &amp; fetch() API</text></svg>`
    },
    {
      title: "Slide 3: Persistență Web",
      svg: `data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="600" height="300" viewBox="0 0 600 300"><defs><linearGradient id="g3" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stop-color="%2310b981"/><stop offset="100%" stop-color="%23047857"/></linearGradient></defs><rect width="100%" height="100%" fill="url(%23g3)"/><text x="50%" y="45%" dominant-baseline="middle" text-anchor="middle" font-family="sans-serif" font-size="28" font-weight="bold" fill="white">3. Persistență de Date</text><text x="50%" y="62%" dominant-baseline="middle" text-anchor="middle" font-family="sans-serif" font-size="16" fill="%23d1fae5">Web Storage: localStorage</text></svg>`
    },
    {
      title: "Slide 4: Paradigma Scriptică",
      svg: `data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="600" height="300" viewBox="0 0 600 300"><defs><linearGradient id="g4" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stop-color="%238b5cf6"/><stop offset="100%" stop-color="%236d28d9"/></linearGradient></defs><rect width="100%" height="100%" fill="url(%23g4)"/><text x="50%" y="45%" dominant-baseline="middle" text-anchor="middle" font-family="sans-serif" font-size="28" font-weight="bold" fill="white">4. Paradigma Scriptică</text><text x="50%" y="62%" dominant-baseline="middle" text-anchor="middle" font-family="sans-serif" font-size="16" fill="%23ede9fe">Laboratorul 1 finalizat cu succes!</text></svg>`
    }
  ];

  let currentIndex = 0;
  let timerId = null;

  // Creează punctele indicatoare (dots)
  dotsContainer.innerHTML = "";
  slides.forEach((_, idx) => {
    const dot = document.createElement("div");
    dot.className = `slider-dot ${idx === 0 ? "active" : ""}`;
    dot.addEventListener("click", () => {
      goToSlide(idx);
      resetSliderTimer();
    });
    dotsContainer.appendChild(dot);
  });

  function updateSlideUI() {
    imgEl.src = slides[currentIndex].svg;
    captionEl.textContent = `${slides[currentIndex].title} (${currentIndex + 1} din ${slides.length})`;

    const dots = dotsContainer.querySelectorAll(".slider-dot");
    dots.forEach((d, i) => {
      d.classList.toggle("active", i === currentIndex);
    });
  }

  function goToSlide(index) {
    currentIndex = (index + slides.length) % slides.length;
    updateSlideUI();
  }

  function nextSlide() {
    goToSlide(currentIndex + 1);
  }

  function prevSlide() {
    goToSlide(currentIndex - 1);
  }

  function startSliderTimer() {
    timerId = setInterval(nextSlide, 3000); // Schimbare la fiecare 3 secunde conform cerinței
  }

  function resetSliderTimer() {
    clearInterval(timerId);
    startSliderTimer();
  }

  btnNext.addEventListener("click", () => {
    nextSlide();
    resetSliderTimer();
  });

  btnPrev.addEventListener("click", () => {
    prevSlide();
    resetSliderTimer();
  });

  // Inițializare
  updateSlideUI();
  startSliderTimer();
}

/**
 * 1.10: Creează un mic calculator cu butoane pentru +, -, ×, ÷ și un ecran pentru rezultat.
 */
function initTask1_10() {
  const display = document.getElementById("calc-display");
  const buttons = document.querySelectorAll(".calc-btn");

  if (!display || buttons.length === 0) return;

  let currentInput = "0";
  let previousValue = null;
  let activeOperator = null;
  let shouldResetDisplay = false;

  function updateDisplay() {
    display.textContent = currentInput;
  }

  function handleNumber(num) {
    if (currentInput === "0" || shouldResetDisplay) {
      currentInput = num;
      shouldResetDisplay = false;
    } else {
      currentInput += num;
    }
  }

  function handleDecimal() {
    if (shouldResetDisplay) {
      currentInput = "0.";
      shouldResetDisplay = false;
      return;
    }
    if (!currentInput.includes(".")) {
      currentInput += ".";
    }
  }

  function handleOperator(op) {
    const val = parseFloat(currentInput);

    if (previousValue === null) {
      previousValue = val;
    } else if (activeOperator) {
      const result = compute(previousValue, val, activeOperator);
      currentInput = String(result);
      previousValue = result;
      updateDisplay();
    }

    activeOperator = op;
    shouldResetDisplay = true;
  }

  function compute(a, b, op) {
    switch (op) {
      case "+": return a + b;
      case "-": return a - b;
      case "*": return a * b;
      case "/":
        if (b === 0) {
          alert("Eroare: Împărțirea la zero nu este permisă!");
          return 0;
        }
        return a / b;
      default: return b;
    }
  }

  function handleEquals() {
    if (activeOperator === null || previousValue === null) return;

    const val = parseFloat(currentInput);
    const result = compute(previousValue, val, activeOperator);

    // Rotunjire inteligentă a zecimalelor dacă este cazul
    currentInput = String(Math.round(result * 100000000) / 100000000);
    previousValue = null;
    activeOperator = null;
    shouldResetDisplay = true;
    updateDisplay();
  }

  function handleClear() {
    currentInput = "0";
    previousValue = null;
    activeOperator = null;
    shouldResetDisplay = false;
    updateDisplay();
  }

  function handleBackspace() {
    if (shouldResetDisplay) return;
    if (currentInput.length > 1) {
      currentInput = currentInput.slice(0, -1);
    } else {
      currentInput = "0";
    }
    updateDisplay();
  }

  buttons.forEach(btn => {
    btn.addEventListener("click", () => {
      const action = btn.dataset.action;
      const val = btn.dataset.val;

      switch (action) {
        case "number":
          handleNumber(val);
          updateDisplay();
          break;
        case "decimal":
          handleDecimal();
          updateDisplay();
          break;
        case "operator":
          handleOperator(val);
          break;
        case "equals":
          handleEquals();
          break;
        case "clear":
          handleClear();
          break;
        case "backspace":
          handleBackspace();
          break;
      }
    });
  });
}


/* ============================================================================
   SARCINA 2: CERINȚE AVANSATE (Nota 10)
   ============================================================================ */

/**
 * 2.1: Generează un număr aleator între 1 și 100 și cere utilizatorului să-l ghicească,
 * afișând "mai mic" sau "mai mare" în dependență de valoarea introdusă.
 */
function initTask2_1() {
  const input = document.getElementById("guess-input");
  const btnGuess = document.getElementById("btn-guess");
  const btnReset = document.getElementById("btn-reset-game");
  const res = document.getElementById("res-guess");

  if (!input || !btnGuess || !btnReset || !res) return;

  let secretNumber = Math.floor(Math.random() * 100) + 1;
  let attempts = 0;
  let gameOver = false;

  function resetGame() {
    secretNumber = Math.floor(Math.random() * 100) + 1;
    attempts = 0;
    gameOver = false;
    input.value = "";
    input.disabled = false;
    btnGuess.disabled = false;
    res.innerHTML = "🎲 Joc nou pornit! Am ales un număr între 1 și 100. Încearcă să-l ghicești!";
  }

  function checkGuess() {
    if (gameOver) return;

    const val = parseInt(input.value.trim(), 10);
    if (isNaN(val) || val < 1 || val > 100) {
      res.innerHTML = "⚠️ Vă rugăm introduceți un număr valid cuprins între 1 și 100!";
      return;
    }

    attempts++;

    if (val === secretNumber) {
      gameOver = true;
      input.disabled = true;
      btnGuess.disabled = true;
      res.innerHTML = `🎉 <strong>FELICITĂRI!</strong> Ai ghicit numărul <strong>${secretNumber}</strong> din <strong>${attempts}</strong> încercări!`;
    } else if (val < secretNumber) {
      res.innerHTML = `⬆️ Numărul secret este <strong>MAI MARE</strong> decât ${val}! (Încercarea #${attempts})`;
    } else {
      res.innerHTML = `⬇️ Numărul secret este <strong>MAI MIC</strong> decât ${val}! (Încercarea #${attempts})`;
    }

    input.value = "";
    input.focus();
  }

  btnGuess.addEventListener("click", checkGuess);
  btnReset.addEventListener("click", resetGame);
  input.addEventListener("keydown", (e) => {
    if (e.key === "Enter") checkGuess();
  });
}

/**
 * 2.2: Creează o pagină care numără câte click-uri s-au făcut pe un buton și afișează numărul în timp real.
 */
function initTask2_2() {
  const btnClick = document.getElementById("btn-click-me");
  const btnReset = document.getElementById("btn-click-reset");
  const display = document.getElementById("click-counter-display");

  if (!btnClick || !btnReset || !display) return;

  let clicks = 0;

  btnClick.addEventListener("click", () => {
    clicks++;
    display.textContent = clicks;
  });

  btnReset.addEventListener("click", () => {
    clicks = 0;
    display.textContent = clicks;
  });
}

/**
 * 2.3 & 2.4: Realizează un sistem de notițe: utilizatorul scrie text într-un input, apasă „Adaugă”,
 * iar nota apare într-o listă. Adaugă un buton de ștergere pentru fiecare notă.
 */
function initTask2_3_4() {
  const input = document.getElementById("note-input");
  const btnAdd = document.getElementById("btn-add-note");
  const list = document.getElementById("notes-list");

  if (!input || !btnAdd || !list) return;

  function addNote() {
    const text = input.value.trim();
    if (text === "") {
      alert("Introduceți textul notiței!");
      return;
    }

    const li = document.createElement("li");

    const spanText = document.createElement("span");
    spanText.textContent = text;
    spanText.style.wordBreak = "break-word";

    const btnDelete = document.createElement("button");
    btnDelete.className = "btn btn-danger btn-sm";
    btnDelete.innerHTML = "🗑️ Șterge";
    btnDelete.setAttribute("title", "Șterge această notă");

    // Sarcina 2.4: Buton de ștergere pentru fiecare notă
    btnDelete.addEventListener("click", () => {
      li.remove();
    });

    li.appendChild(spanText);
    li.appendChild(btnDelete);
    list.appendChild(li);

    input.value = "";
    input.focus();
  }

  btnAdd.addEventListener("click", addNote);
  input.addEventListener("keydown", (e) => {
    if (e.key === "Enter") addNote();
  });
}

/**
 * 2.5: Implementează un cronometru (timer) care permite pornire, pauză și resetare,
 * folosind setInterval() și clearInterval().
 */
function initTask2_5() {
  const display = document.getElementById("stopwatch-display");
  const btnStart = document.getElementById("btn-timer-start");
  const btnPause = document.getElementById("btn-timer-pause");
  const btnReset = document.getElementById("btn-timer-reset");
  const status = document.getElementById("stopwatch-status");

  if (!display || !btnStart || !btnPause || !btnReset || !status) return;

  let timerInterval = null;
  let totalSeconds = 0;

  function formatTime(seconds) {
    const h = String(Math.floor(seconds / 3600)).padStart(2, "0");
    const m = String(Math.floor((seconds % 3600) / 60)).padStart(2, "0");
    const s = String(seconds % 60).padStart(2, "0");
    return `${h}:${m}:${s}`;
  }

  function updateDisplay() {
    display.textContent = formatTime(totalSeconds);
  }

  btnStart.addEventListener("click", () => {
    if (timerInterval !== null) return; // Deja pornit

    status.innerHTML = "Stare: <strong style='color: var(--success-color);'>În derulare (setInterval activ)</strong>";
    timerInterval = setInterval(() => {
      totalSeconds++;
      updateDisplay();
    }, 1000);
  });

  btnPause.addEventListener("click", () => {
    if (timerInterval === null) return; // Deja în pauză

    clearInterval(timerInterval); // Utilizare clearInterval()
    timerInterval = null;
    status.innerHTML = "Stare: <strong style='color: var(--warning-color);'>Pauză (clearInterval apelat)</strong>";
  });

  btnReset.addEventListener("click", () => {
    clearInterval(timerInterval); // Utilizare clearInterval()
    timerInterval = null;
    totalSeconds = 0;
    updateDisplay();
    status.innerHTML = "Stare: Oprit și resetat la 00:00:00";
  });
}

/**
 * 2.6 & 2.7: Mod întunecat (dark mode): schimbă culorile de fundal și text simultan,
 * și salvează automat în localStorage tema aleasă de utilizator, aplicând-o la reîncărcare.
 */
function initTheme() {
  const savedTheme = localStorage.getItem("app_theme");
  if (savedTheme === "dark") {
    document.body.classList.add("dark-theme");
  } else {
    document.body.classList.remove("dark-theme");
  }
  updateThemeControlsUI();
}

function updateThemeControlsUI() {
  const isDark = document.body.classList.contains("dark-theme");
  const themeIcon = document.getElementById("theme-icon");
  const themeText = document.getElementById("theme-text");
  const statusBox = document.getElementById("theme-status-box");

  if (themeIcon) themeIcon.textContent = isDark ? "☀️" : "🌙";
  if (themeText) themeText.textContent = isDark ? "Mod Luminos" : "Mod Întunecat";
  if (statusBox) {
    statusBox.innerHTML = `Tema curentă: <strong>${isDark ? "Mod Întunecat (Dark Mode)" : "Mod Luminos (Light Mode)"}</strong> (Salvat în <code>localStorage</code>)`;
  }
}

function toggleTheme() {
  const isDark = document.body.classList.toggle("dark-theme");
  // Salvare automată în localStorage (Sarcina 2.7)
  localStorage.setItem("app_theme", isDark ? "dark" : "light");
  updateThemeControlsUI();
}

function initTask2_6_7() {
  const headerBtn = document.getElementById("theme-toggle-btn");
  const cardBtn = document.getElementById("btn-toggle-dark-card");
  const clearBtn = document.getElementById("btn-clear-theme");

  if (headerBtn) headerBtn.addEventListener("click", toggleTheme);
  if (cardBtn) cardBtn.addEventListener("click", toggleTheme);

  if (clearBtn) {
    clearBtn.addEventListener("click", () => {
      localStorage.removeItem("app_theme");
      document.body.classList.remove("dark-theme");
      updateThemeControlsUI();
      alert("Preferința din localStorage a fost ștearsă!");
    });
  }

  updateThemeControlsUI();
}

/**
 * 2.8: Listă de sarcini (To-Do List) unde poți adăuga, bifa și șterge elemente,
 * cu salvare automată în localStorage.
 */
function initTask2_8() {
  const input = document.getElementById("todo-input");
  const btnAdd = document.getElementById("btn-add-todo");
  const listContainer = document.getElementById("todo-list-container");
  const countEl = document.getElementById("todo-count");
  const btnClearAll = document.getElementById("btn-clear-all-todos");

  if (!input || !btnAdd || !listContainer || !countEl) return;

  const STORAGE_KEY = "lab1_todos";

  // Citire din localStorage
  function loadTodos() {
    try {
      const data = localStorage.getItem(STORAGE_KEY);
      return data ? JSON.parse(data) : [];
    } catch {
      return [];
    }
  }

  // Salvare în localStorage
  function saveTodos(todos) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(todos));
  }

  let todos = loadTodos();

  function renderTodos() {
    listContainer.innerHTML = "";

    if (todos.length === 0) {
      listContainer.innerHTML = "<li style='color: var(--text-muted); font-style: italic; justify-content: center;'>Nu există sarcini în listă.</li>";
      countEl.textContent = "0 sarcini";
      return;
    }

    const completedCount = todos.filter(t => t.completed).length;
    countEl.textContent = `${completedCount}/${todos.length} completate`;

    todos.forEach(todo => {
      const li = document.createElement("li");
      li.className = `todo-item ${todo.completed ? "completed" : ""}`;

      const textWrap = document.createElement("div");
      textWrap.className = "todo-text-wrap";

      // Checkbox pentru bifat
      const checkbox = document.createElement("input");
      checkbox.type = "checkbox";
      checkbox.checked = todo.completed;
      checkbox.addEventListener("change", () => {
        todo.completed = checkbox.checked;
        saveTodos(todos);
        renderTodos();
      });

      const span = document.createElement("span");
      span.className = "todo-text";
      span.textContent = todo.text;

      textWrap.appendChild(checkbox);
      textWrap.appendChild(span);

      // Buton de ștergere
      const btnDel = document.createElement("button");
      btnDel.className = "btn btn-danger btn-sm";
      btnDel.innerHTML = "×";
      btnDel.setAttribute("title", "Șterge sarcina");
      btnDel.addEventListener("click", () => {
        todos = todos.filter(t => t.id !== todo.id);
        saveTodos(todos);
        renderTodos();
      });

      li.appendChild(textWrap);
      li.appendChild(btnDel);
      listContainer.appendChild(li);
    });
  }

  function addTodo() {
    const text = input.value.trim();
    if (text === "") {
      alert("Introduceți o denumire pentru sarcină!");
      return;
    }

    const newTodo = {
      id: Date.now(),
      text: text,
      completed: false
    };

    todos.push(newTodo);
    saveTodos(todos);
    renderTodos();

    input.value = "";
    input.focus();
  }

  btnAdd.addEventListener("click", addTodo);
  input.addEventListener("keydown", (e) => {
    if (e.key === "Enter") addTodo();
  });

  if (btnClearAll) {
    btnClearAll.addEventListener("click", () => {
      if (todos.length === 0) return;
      if (confirm("Sigur doriți să ștergeți toate sarcinile din listă?")) {
        todos = [];
        saveTodos(todos);
        renderTodos();
      }
    });
  }

  // Render inițial
  renderTodos();
}

/**
 * 2.9: Creează un script care preia date despre vreme folosind fetch() de la un API și le afișează pe pagină.
 * Se folosește API-ul public gratuit Open-Meteo (fără necesitate de API key).
 */
function initTask2_9() {
  const select = document.getElementById("weather-city-select");
  const btn = document.getElementById("btn-fetch-weather");
  const container = document.getElementById("weather-result-container");

  if (!select || !btn || !container) return;

  // Mapare coduri meteo WMO la descrieri în română și emoticoane
  function getWeatherDescription(code) {
    switch (code) {
      case 0: return { label: "Cer senin", icon: "☀️" };
      case 1:
      case 2: return { label: "Parțial înnorat", icon: "🌤️" };
      case 3: return { label: "Înnorat", icon: "☁️" };
      case 45:
      case 48: return { label: "Ceață", icon: "🌫️" };
      case 51:
      case 53:
      case 55: return { label: "Burniță ușoară", icon: "🌦️" };
      case 61:
      case 63:
      case 65: return { label: "Ploaie", icon: "🌧️" };
      case 71:
      case 73:
      case 75: return { label: "Ninsoare", icon: "❄️" };
      case 80:
      case 81:
      case 82: return { label: "Averse de ploaie", icon: "🌧️" };
      case 95: return { label: "Furtună cu descărcări electrice", icon: "⛈️" };
      default: return { label: "Vreme variabilă", icon: "⛅" };
    }
  }

  async function fetchWeather() {
    const val = select.value.split(",");
    const lat = val[0];
    const lon = val[1];
    const cityName = val[2];

    container.innerHTML = `
      <div class="result-box">
        ⏳ Se preiau datele meteo pentru <strong>${cityName}</strong> prin <code>fetch()</code>...
      </div>
    `;

    const apiUrl = `https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m&timezone=auto`;

    try {
      const response = await fetch(apiUrl);
      if (!response.ok) {
        throw new Error(`Răspuns server HTTP ${response.status}`);
      }

      const data = await response.json();
      const cur = data.current;
      const weatherInfo = getWeatherDescription(cur.weather_code);

      container.innerHTML = `
        <div class="weather-card">
          <div class="weather-header">
            <div>
              <strong style="font-size: 1.15rem;">📍 ${cityName}</strong>
              <div style="font-size: 0.85rem; opacity: 0.9;">${weatherInfo.icon} ${weatherInfo.label}</div>
            </div>
            <div class="weather-temp">${Math.round(cur.temperature_2m)}°C</div>
          </div>
          <div class="weather-details">
            <div>🌡️ Resimțit: <strong>${Math.round(cur.apparent_temperature)}°C</strong></div>
            <div>💧 Umiditate: <strong>${cur.relative_humidity_2m}%</strong></div>
            <div>💨 Vânt: <strong>${cur.wind_speed_10m} km/h</strong></div>
            <div>🌧️ Precipitații: <strong>${cur.precipitation} mm</strong></div>
          </div>
          <div style="font-size: 0.7rem; opacity: 0.8; text-align: right;">
            Sursă: Open-Meteo API | Actualizat: ${new Date(cur.time).toLocaleTimeString("ro-RO")}
          </div>
        </div>
      `;
    } catch (error) {
      container.innerHTML = `
        <div class="result-box" style="border-color: var(--danger-color); color: var(--danger-color);">
          ⚠️ <strong>Eroare la preluarea vremii:</strong> ${error.message}.
          <br><small style="color: var(--text-secondary); margin-top: 4px;">Verificați conexiunea la internet sau încercați din nou.</small>
        </div>
      `;
    }
  }

  btn.addEventListener("click", fetchWeather);
}

/**
 * 2.10: Scrie o funcție asincronă care citește un fișier JSON local și afișează conținutul într-un tabel HTML.
 */
async function loadLocalJSON(fileOrPath = "data.json") {
  const feedback = document.getElementById("json-feedback");
  const tableContainer = document.getElementById("json-table-container");

  if (!feedback || !tableContainer) return;

  feedback.innerHTML = "⏳ Se citește fișierul JSON în mod asincron (<code>async / await</code>)...";

  try {
    let jsonData;

    // Cazul în care a fost selectat manual un fișier prin input file (pt. dublu-click direct file:// fără server)
    if (fileOrPath instanceof File) {
      const fileText = await fileOrPath.text();
      jsonData = JSON.parse(fileText);
    } else {
      // Citire standard asincronă prin fetch()
      const response = await fetch(fileOrPath);
      if (!response.ok) {
        throw new Error(`Eroare HTTP la încărcarea ${fileOrPath}: statut ${response.status}`);
      }
      jsonData = await response.json();
    }

    if (!Array.isArray(jsonData) || jsonData.length === 0) {
      feedback.innerHTML = "⚠️ Fișierul JSON nu conține o listă validă de elemente.";
      tableContainer.style.display = "none";
      return;
    }

    // Generare dinamică tabel HTML
    renderJSONTable(jsonData);

    feedback.innerHTML = `✅ Datele din <strong>${fileOrPath instanceof File ? fileOrPath.name : fileOrPath}</strong> au fost citite cu succes (${jsonData.length} înregistrări)!`;
    tableContainer.style.display = "block";

  } catch (err) {
    console.warn("Eroare la fetch local:", err);

    // În browserele moderne, deschiderea fișierului direct prin protocolul file://
    // restricționează fetch('data.json') din motive de securitate CORS.
    // Oferim un fallback demonstrativ complet cu datele din data.json:
    const fallbackData = [
      {
        "id": 1,
        "nume": "Ion Creangă",
        "disciplina": "Paradigma Scriptică",
        "laborator": "Laboratorul 1 - JS",
        "nota": 10,
        "status": "Promovat",
        "data_predare": "2026-09-15"
      },
      {
        "id": 2,
        "nume": "Mihai Eminescu",
        "disciplina": "Paradigma Scriptică",
        "laborator": "Laboratorul 1 - JS",
        "nota": 10,
        "status": "Promovat",
        "data_predare": "2026-09-15"
      },
      {
        "id": 3,
        "nume": "Vasile Alecsandri",
        "disciplina": "Paradigma Scriptică",
        "laborator": "Laboratorul 1 - JS",
        "nota": 9,
        "status": "Promovat",
        "data_predare": "2026-09-14"
      },
      {
        "id": 4,
        "nume": "George Coșbuc",
        "disciplina": "Paradigma Scriptică",
        "laborator": "Laboratorul 1 - JS",
        "nota": 8,
        "status": "Promovat",
        "data_predare": "2026-09-13"
      },
      {
        "id": 5,
        "nume": "Lucian Blaga",
        "disciplina": "Paradigma Scriptică",
        "laborator": "Laboratorul 1 - JS",
        "nota": 10,
        "status": "Promovat",
        "data_predare": "2026-09-15"
      }
    ];

    renderJSONTable(fallbackData);
    tableContainer.style.display = "block";
    feedback.innerHTML = `
      ℹ️ <em>Notă CORS (dacă pagina e deschisă prin <code>file://</code>):</em><br>
      S-a activat mecanismul de fallback cu datele identice din <code>data.json</code>.<br>
      Când pagina este servită prin HTTP (Live Server, npx serve sau Python), <code>fetch('data.json')</code> funcționează direct pe server.
    `;
  }
}

/**
 * Funcție auxiliară pentru afișarea conținutului JSON într-un tabel HTML
 */
function renderJSONTable(data) {
  const tableContainer = document.getElementById("json-table-container");
  if (!tableContainer || data.length === 0) return;

  const headers = Object.keys(data[0]);

  let tableHtml = `
    <table class="data-table">
      <thead>
        <tr>
          ${headers.map(h => `<th>${h.toUpperCase()}</th>`).join("")}
        </tr>
      </thead>
      <tbody>
  `;

  data.forEach(item => {
    tableHtml += `<tr>`;
    headers.forEach(key => {
      const val = item[key];
      if (key === "status" && String(val).toLowerCase() === "promovat") {
        tableHtml += `<td><span class="status-badge">${val}</span></td>`;
      } else if (key === "nota") {
        tableHtml += `<td><strong>${val}</strong></td>`;
      } else {
        tableHtml += `<td>${val}</td>`;
      }
    });
    tableHtml += `</tr>`;
  });

  tableHtml += `
      </tbody>
    </table>
  `;

  tableContainer.innerHTML = tableHtml;
}

function initTask2_10() {
  const btn = document.getElementById("btn-load-json");
  const fileInput = document.getElementById("json-file-input");

  if (btn) {
    btn.addEventListener("click", () => {
      loadLocalJSON("data.json");
    });
  }

  if (fileInput) {
    fileInput.addEventListener("change", (e) => {
      const file = e.target.files[0];
      if (file) {
        loadLocalJSON(file);
      }
    });
  }
}
