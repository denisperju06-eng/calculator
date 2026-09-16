/**
 * Automated Verification Script for OOP Calculator Engine
 */
global.window = {};

require('./js/errors/CalculatorErrors.js');
require('./js/operations/BaseOperation.js');
require('./js/operations/ArithmeticOps.js');
require('./js/operations/AdditionalOps.js');
require('./js/operations/TrigOps.js');
require('./js/operations/LogOps.js');
require('./js/core/MemoryUnit.js');
require('./js/core/BaseConverter.js');
require('./js/core/CalculatorEngine.js');

const assert = require('assert');

console.log("=== Începere Teste Calculator OOP ===");

const engine = new window.CalculatorEngine();

// --- Cerința a: Operații aritmetice de bază pe numere reale ---
console.log("\n[Test Cerința a: Operații aritmetice de bază]");
engine.clear();
engine.inputDigit('1');
engine.inputDigit('2');
engine.inputDecimal();
engine.inputDigit('5'); // 12.5
engine.setBinaryOperator('+');
engine.inputDigit('7');
engine.inputDecimal();
engine.inputDigit('5'); // 7.5
engine.calculateEquals();
assert.strictEqual(engine.getState().displayValue, '20');
console.log("✓ Adunare: 12.5 + 7.5 = 20 PASSED");

engine.setBinaryOperator('*');
engine.inputDigit('3');
engine.calculateEquals();
assert.strictEqual(engine.getState().displayValue, '60');
console.log("✓ Înmulțire: 20 * 3 = 60 PASSED");

engine.setBinaryOperator('/');
engine.inputDigit('4');
engine.calculateEquals();
assert.strictEqual(engine.getState().displayValue, '15');
console.log("✓ Împărțire: 60 / 4 = 15 PASSED");

engine.setBinaryOperator('-');
engine.inputDigit('5');
engine.calculateEquals();
assert.strictEqual(engine.getState().displayValue, '10');
console.log("✓ Scădere: 15 - 5 = 10 PASSED");

engine.setBinaryOperator('mod');
engine.inputDigit('3');
engine.calculateEquals();
assert.strictEqual(engine.getState().displayValue, '1');
console.log("✓ Modulo: 10 mod 3 = 1 PASSED");

// --- Cerința b: Minim 3 operații adăugătoare ---
console.log("\n[Test Cerința b: Operații adăugătoare]");
engine.clear();
engine.inputDigit('1');
engine.inputDigit('6'); // 16
engine.applyUnaryOperator('sqrt'); // √16 = 4
assert.strictEqual(engine.getState().displayValue, '4');
console.log("✓ Radical √16 = 4 PASSED");

engine.applyUnaryOperator('sqr'); // 4² = 16
assert.strictEqual(engine.getState().displayValue, '16');
console.log("✓ Ridicare la pătrat 4² = 16 PASSED");

engine.clear();
engine.inputDigit('5');
engine.applyUnaryOperator('fact'); // 5! = 120
assert.strictEqual(engine.getState().displayValue, '120');
console.log("✓ Factorial 5! = 120 PASSED");

engine.clear();
engine.inputDigit('2');
engine.setBinaryOperator('pow');
engine.inputDigit('8');
engine.calculateEquals(); // 2^8 = 256
assert.strictEqual(engine.getState().displayValue, '256');
console.log("✓ Ridicare la putere 2^8 = 256 PASSED");

engine.clear();
engine.inputDigit('2');
engine.inputDigit('7');
engine.applyUnaryOperator('cbrt'); // ∛27 = 3
assert.strictEqual(engine.getState().displayValue, '3');
console.log("✓ Radical de ordin 3 ∛27 = 3 PASSED");

engine.clear();
engine.inputDigit('5');
engine.inputDigit('0');
engine.applyUnaryOperator('pct'); // 50% = 0.5
assert.strictEqual(engine.getState().displayValue, '0.5');
console.log("✓ Procent 50% = 0.5 PASSED");

// --- Cerința c: Trigonometrice & Logaritmice ---
console.log("\n[Test Cerința c: Trigonometrice & Logaritmice]");
engine.clear();
engine.setAngleMode('DEG');
engine.inputDigit('9');
engine.inputDigit('0');
engine.applyUnaryOperator('sin'); // sin(90°) = 1
assert.strictEqual(engine.getState().displayValue, '1');
console.log("✓ sin(90°) = 1 PASSED");

engine.clear();
engine.inputDigit('0');
engine.applyUnaryOperator('cos'); // cos(0°) = 1
assert.strictEqual(engine.getState().displayValue, '1');
console.log("✓ cos(0°) = 1 PASSED");

engine.clear();
engine.inputDigit('4');
engine.inputDigit('5');
engine.applyUnaryOperator('tan'); // tan(45°) = 1
assert.strictEqual(Math.round(parseFloat(engine.getState().displayValue)), 1);
console.log("✓ tan(45°) ≈ 1 PASSED");

// Logaritmi: ln, log10, log2
engine.clear();
engine.inputDigit('1');
engine.inputDigit('0');
engine.inputDigit('0');
engine.applyUnaryOperator('log'); // log10(100) = 2
assert.strictEqual(engine.getState().displayValue, '2');
console.log("✓ log10(100) = 2 PASSED");

engine.clear();
engine.inputDigit('8');
engine.applyUnaryOperator('log2'); // log2(8) = 3
assert.strictEqual(engine.getState().displayValue, '3');
console.log("✓ log2(8) = 3 PASSED");

engine.clear();
engine.inputConstant('e');
engine.applyUnaryOperator('ln'); // ln(e) = 1
assert.strictEqual(engine.getState().displayValue, '1');
console.log("✓ ln(e) = 1 PASSED");

// --- Cerința d: Operații cu memoria ---
console.log("\n[Test Cerința d: Operații cu memoria]");
engine.clear();
engine.memoryClear();
assert.strictEqual(engine.getState().hasMemory, false);

engine.inputDigit('4');
engine.inputDigit('2');
engine.memoryStore(); // MS: 42
assert.strictEqual(engine.getState().hasMemory, true);
assert.strictEqual(engine.getState().memoryValue, 42);
console.log("✓ MS (Memory Store) = 42 PASSED");

engine.clear();
assert.strictEqual(engine.getState().displayValue, '0');
engine.memoryRecall(); // MR: 42
assert.strictEqual(engine.getState().displayValue, '42');
console.log("✓ MR (Memory Recall) = 42 PASSED");

engine.clear();
engine.inputDigit('8');
engine.memoryAdd(); // M+: 42 + 8 = 50
assert.strictEqual(engine.getState().memoryValue, 50);
console.log("✓ M+ (Memory Add): 42 + 8 = 50 PASSED");

engine.clear();
engine.inputDigit('5');
engine.memorySubtract(); // M-: 50 - 5 = 45
assert.strictEqual(engine.getState().memoryValue, 45);
console.log("✓ M- (Memory Subtract): 50 - 5 = 45 PASSED");

engine.memoryClear();
assert.strictEqual(engine.getState().hasMemory, false);
console.log("✓ MC (Memory Clear) PASSED");

// --- Cerința e: Prelucrarea erorilor de calcule ---
console.log("\n[Test Cerința e: Prelucrarea erorilor de calcule]");
// 1. Împărțire la zero
engine.clear();
engine.inputDigit('1');
engine.inputDigit('0');
engine.setBinaryOperator('/');
engine.inputDigit('0');
engine.calculateEquals();
assert.strictEqual(engine.getState().hasError, true);
console.log(`✓ Tratare eroare împărțire la zero: "${engine.getState().errorMessage}" PASSED`);

// 2. Radical din număr negativ
engine.clear();
engine.inputDigit('9');
engine.toggleSign(); // -9
engine.applyUnaryOperator('sqrt');
assert.strictEqual(engine.getState().hasError, true);
console.log(`✓ Tratare eroare radical din negativ: "${engine.getState().errorMessage}" PASSED`);

// 3. Logaritm din număr negativ sau 0
engine.clear();
engine.inputDigit('0');
engine.applyUnaryOperator('ln');
assert.strictEqual(engine.getState().hasError, true);
console.log(`✓ Tratare eroare ln(0): "${engine.getState().errorMessage}" PASSED`);

// 4. Tangentă la 90 grade
engine.clear();
engine.setAngleMode('DEG');
engine.inputDigit('9');
engine.inputDigit('0');
engine.applyUnaryOperator('tan');
assert.strictEqual(engine.getState().hasError, true);
console.log(`✓ Tratare eroare tan(90°): "${engine.getState().errorMessage}" PASSED`);

// --- Cerința f: Transformarea hexazecimală și sisteme de numerație ---
console.log("\n[Test Cerința f: Transformare hexazecimală și alte sisteme]");
const converter = new window.BaseConverter();

// Test zecimal 255 -> HEX: FF, OCT: 377, BIN: 1111 1111
const bases255 = converter.getAllBases(255);
assert.strictEqual(bases255.HEX, 'FF');
assert.strictEqual(bases255.DEC, '255');
assert.strictEqual(bases255.OCT, '377');
assert.strictEqual(bases255.BIN, '1111 1111');
console.log("✓ 255 DEC => HEX: FF, OCT: 377, BIN: 1111 1111 PASSED");

// Test hex -> dec
const decFromHex = converter.parseToDecimal('1A3F', 'HEX');
assert.strictEqual(decFromHex, 6719);
console.log("✓ 1A3F HEX => 6719 DEC PASSED");

// Test bin -> dec
const decFromBin = converter.parseToDecimal('10110', 'BIN');
assert.strictEqual(decFromBin, 22);
console.log("✓ 10110 BIN => 22 DEC PASSED");

// Test conversie în engine
engine.clear();
engine.inputDigit('2');
engine.inputDigit('5');
engine.inputDigit('5');
engine.setBase('HEX');
assert.strictEqual(engine.getState().displayValue, 'FF');
console.log("✓ Engine setBase('HEX'): 255 -> FF PASSED");

engine.setBase('BIN');
assert.strictEqual(engine.getState().displayValue, '11111111');
console.log("✓ Engine setBase('BIN'): 255 -> 11111111 PASSED");

console.log("\n=======================================================");
console.log("🎉 TOATE TESTELE AU TRECUT CU SUCCES (Cerințele a-f)!");
console.log("=======================================================");
