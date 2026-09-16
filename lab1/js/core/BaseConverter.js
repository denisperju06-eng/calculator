(() => {
    const _scope = typeof window !== 'undefined' ? window : (typeof global !== 'undefined' ? global : globalThis);

    class BaseConverter {
    constructor() {
        this._supportedBases = {
            HEX: 16,
            DEC: 10,
            OCT: 8,
            BIN: 2
        };
    }

    /**
     * Validează dacă o valoare șir de caractere este validă într-o anumită bază
     * @param {string} str
     * @param {number|string} base (2, 8, 10, 16 sau 'BIN', 'OCT', 'DEC', 'HEX')
     */
    isValidInBase(str, base) {
        if (!str || typeof str !== 'string') return false;
        const b = typeof base === 'string' ? this._supportedBases[base.toUpperCase()] : base;
        
        // Permite semnul minus opțional la început
        const clean = str.startsWith('-') ? str.slice(1) : str;
        if (clean.length === 0) return false;

        switch (b) {
            case 2:
                return /^[01]+(\.[01]+)?$/.test(clean);
            case 8:
                return /^[0-7]+(\.[0-7]+)?$/.test(clean);
            case 10:
                return /^[0-9]+(\.[0-9]+)?$/.test(clean);
            case 16:
                return /^[0-9A-Fa-f]+(\.[0-9A-Fa-f]+)?$/.test(clean);
            default:
                return false;
        }
    }

    /**
     * Convertește un număr zecimal în hexazecimal (format standard cu majuscule)
     * @param {number} decimalValue
     * @returns {string}
     */
    toHex(decimalValue) {
        if (!Number.isFinite(decimalValue)) return "0";
        // Convertim partea întreagă folosind BigInt pentru a evita limitările de precizie la numere mari
        const isNegative = decimalValue < 0;
        const absVal = Math.abs(decimalValue);
        const intPart = Math.floor(absVal);
        
        let hexStr = BigInt(intPart).toString(16).toUpperCase();
        
        // Dacă există parte fracționară
        const fracPart = absVal - intPart;
        if (fracPart > 1e-9) {
            let fracHex = "";
            let rem = fracPart;
            for (let i = 0; i < 4 && rem > 0; i++) {
                rem *= 16;
                const digit = Math.floor(rem);
                fracHex += digit.toString(16).toUpperCase();
                rem -= digit;
            }
            hexStr += "." + fracHex;
        }

        return (isNegative ? "-" : "") + hexStr;
    }

    /**
     * Convertește un număr zecimal în octal
     * @param {number} decimalValue
     * @returns {string}
     */
    toOct(decimalValue) {
        if (!Number.isFinite(decimalValue)) return "0";
        const isNegative = decimalValue < 0;
        const absVal = Math.abs(decimalValue);
        const intPart = Math.floor(absVal);
        let octStr = BigInt(intPart).toString(8);
        return (isNegative ? "-" : "") + octStr;
    }

    /**
     * Convertește un număr zecimal în binar (cu spațiere per grup de 4 biți)
     * @param {number} decimalValue
     * @param {boolean} formatWithSpaces
     * @returns {string}
     */
    toBin(decimalValue, formatWithSpaces = true) {
        if (!Number.isFinite(decimalValue)) return "0";
        const isNegative = decimalValue < 0;
        const absVal = Math.abs(decimalValue);
        const intPart = Math.floor(absVal);
        
        let rawBin = BigInt(intPart).toString(2);

        if (formatWithSpaces && rawBin.length > 4) {
            // Grupare în nibble-uri de câte 4 biți
            const padLen = Math.ceil(rawBin.length / 4) * 4;
            const padded = rawBin.padStart(padLen, '0');
            const chunks = padded.match(/.{1,4}/g);
            rawBin = chunks ? chunks.join(' ') : rawBin;
        }

        return (isNegative ? "-" : "") + rawBin;
    }

    /**
     * Parsează un șir dintr-o bază oarecare (HEX, BIN, OCT, DEC) în număr real zecimal
     * @param {string} valueStr
     * @param {string|number} fromBase
     * @returns {number}
     */
    parseToDecimal(valueStr, fromBase) {
        const cleanStr = valueStr.replace(/\s+/g, '').trim();
        const baseNum = typeof fromBase === 'string' ? this._supportedBases[fromBase.toUpperCase()] : fromBase;

        if (!this.isValidInBase(cleanStr, baseNum)) {
            const { InvalidBaseConversionError } = _scope.CalculatorErrors;
            throw new InvalidBaseConversionError(cleanStr, fromBase);
        }

        if (cleanStr.includes('.')) {
            const [intStr, fracStr] = cleanStr.split('.');
            let intVal = parseInt(intStr, baseNum);
            let fracVal = 0;
            for (let i = 0; i < fracStr.length; i++) {
                const digitVal = parseInt(fracStr[i], baseNum);
                fracVal += digitVal / Math.pow(baseNum, i + 1);
            }
            return (intVal >= 0 ? 1 : -1) * (Math.abs(intVal) + fracVal);
        } else {
            return parseInt(cleanStr, baseNum);
        }
    }

    /**
     * Returnează o reprezentare completă a numărului în toate cele 4 sisteme
     * @param {number} decimalValue
     */
    getAllBases(decimalValue) {
        const num = Number(decimalValue);
        if (!Number.isFinite(num)) {
            return {
                HEX: "0",
                DEC: "0",
                OCT: "0",
                BIN: "0"
            };
        }

        return {
            HEX: this.toHex(num),
            DEC: Math.floor(num).toString(10),
            OCT: this.toOct(num),
            BIN: this.toBin(num, true)
        };
    }
}

    _scope.BaseConverter = BaseConverter;
})();
