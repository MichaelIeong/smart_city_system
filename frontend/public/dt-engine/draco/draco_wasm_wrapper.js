var $jscomp = $jscomp || {};
$jscomp.scope = {};
$jscomp.arrayIteratorImpl = function (f) {
  var m = 0;
  return function () {
    return m < f.length ? { done: !1, value: f[m++] } : { done: !0 };
  };
};
$jscomp.arrayIterator = function (f) {
  return { next: $jscomp.arrayIteratorImpl(f) };
};
$jscomp.makeIterator = function (f) {
  var m = "undefined" != typeof Symbol && Symbol.iterator && f[Symbol.iterator];
  return m ? m.call(f) : $jscomp.arrayIterator(f);
};
$jscomp.getGlobal = function (f) {
  return "undefined" != typeof window && window === f
    ? f
    : "undefined" != typeof global && null != global
    ? global
    : f;
};
$jscomp.global = $jscomp.getGlobal(this);
$jscomp.ASSUME_ES5 = !1;
$jscomp.ASSUME_NO_NATIVE_MAP = !1;
$jscomp.ASSUME_NO_NATIVE_SET = !1;
$jscomp.SIMPLE_FROUND_POLYFILL = !1;
$jscomp.defineProperty =
  $jscomp.ASSUME_ES5 || "function" == typeof Object.defineProperties
    ? Object.defineProperty
    : function (f, m, v) {
        f != Array.prototype && f != Object.prototype && (f[m] = v.value);
      };
$jscomp.polyfill = function (f, m, v, t) {
  if (m) {
    v = $jscomp.global;
    f = f.split(".");
    for (t = 0; t < f.length - 1; t++) {
      var h = f[t];
      h in v || (v[h] = {});
      v = v[h];
    }
    f = f[f.length - 1];
    t = v[f];
    m = m(t);
    m != t &&
      null != m &&
      $jscomp.defineProperty(v, f, { configurable: !0, writable: !0, value: m });
  }
};
$jscomp.FORCE_POLYFILL_PROMISE = !1;
$jscomp.polyfill(
  "Promise",
  function (f) {
    function m() {
      this.batch_ = null;
    }
    function v(e) {
      return e instanceof h
        ? e
        : new h(function (l, f) {
            l(e);
          });
    }
    if (f && !$jscomp.FORCE_POLYFILL_PROMISE) return f;
    m.prototype.asyncExecute = function (e) {
      if (null == this.batch_) {
        this.batch_ = [];
        var l = this;
        this.asyncExecuteFunction(function () {
          l.executeBatch_();
        });
      }
      this.batch_.push(e);
    };
    var t = $jscomp.global.setTimeout;
    m.prototype.asyncExecuteFunction = function (e) {
      t(e, 0);
    };
    m.prototype.executeBatch_ = function () {
      for (; this.batch_ && this.batch_.length; ) {
        var e = this.batch_;
        this.batch_ = [];
        for (var l = 0; l < e.length; ++l) {
          var f = e[l];
          e[l] = null;
          try {
            f();
          } catch (z) {
            this.asyncThrow_(z);
          }
        }
      }
      this.batch_ = null;
    };
    m.prototype.asyncThrow_ = function (e) {
      this.asyncExecuteFunction(function () {
        throw e;
      });
    };
    var h = function (e) {
      this.state_ = 0;
      this.result_ = void 0;
      this.onSettledCallbacks_ = [];
      var l = this.createResolveAndReject_();
      try {
        e(l.resolve, l.reject);
      } catch (S) {
        l.reject(S);
      }
    };
    h.prototype.createResolveAndReject_ = function () {
      function e(e) {
        return function (h) {
          f || ((f = !0), e.call(l, h));
        };
      }
      var l = this,
        f = !1;
      return { resolve: e(this.resolveTo_), reject: e(this.reject_) };
    };
    h.prototype.resolveTo_ = function (e) {
      if (e === this) this.reject_(new TypeError("A Promise cannot resolve to itself"));
      else if (e instanceof h) this.settleSameAsPromise_(e);
      else {
        a: switch (typeof e) {
          case "object":
            var l = null != e;
            break a;
          case "function":
            l = !0;
            break a;
          default:
            l = !1;
        }
        l ? this.resolveToNonPromiseObj_(e) : this.fulfill_(e);
      }
    };
    h.prototype.resolveToNonPromiseObj_ = function (e) {
      var l = void 0;
      try {
        l = e.then;
      } catch (S) {
        this.reject_(S);
        return;
      }
      "function" == typeof l ? this.settleSameAsThenable_(l, e) : this.fulfill_(e);
    };
    h.prototype.reject_ = function (e) {
      this.settle_(2, e);
    };
    h.prototype.fulfill_ = function (e) {
      this.settle_(1, e);
    };
    h.prototype.settle_ = function (e, l) {
      if (0 != this.state_)
        throw Error(
          "Cannot settle(" + e + ", " + l + "): Promise already settled in state" + this.state_
        );
      this.state_ = e;
      this.result_ = l;
      this.executeOnSettledCallbacks_();
    };
    h.prototype.executeOnSettledCallbacks_ = function () {
      if (null != this.onSettledCallbacks_) {
        for (var e = 0; e < this.onSettledCallbacks_.length; ++e)
          X.asyncExecute(this.onSettledCallbacks_[e]);
        this.onSettledCallbacks_ = null;
      }
    };
    var X = new m();
    h.prototype.settleSameAsPromise_ = function (e) {
      var l = this.createResolveAndReject_();
      e.callWhenSettled_(l.resolve, l.reject);
    };
    h.prototype.settleSameAsThenable_ = function (e, l) {
      var f = this.createResolveAndReject_();
      try {
        e.call(l, f.resolve, f.reject);
      } catch (z) {
        f.reject(z);
      }
    };
    h.prototype.then = function (e, f) {
      function l(e, f) {
        return "function" == typeof e
          ? function (f) {
              try {
                m(e(f));
              } catch (p) {
                v(p);
              }
            }
          : f;
      }
      var m,
        v,
        t = new h(function (e, f) {
          m = e;
          v = f;
        });
      this.callWhenSettled_(l(e, m), l(f, v));
      return t;
    };
    h.prototype.catch = function (e) {
      return this.then(void 0, e);
    };
    h.prototype.callWhenSettled_ = function (e, f) {
      function l() {
        switch (h.state_) {
          case 1:
            e(h.result_);
            break;
          case 2:
            f(h.result_);
            break;
          default:
            throw Error("Unexpected state: " + h.state_);
        }
      }
      var h = this;
      null == this.onSettledCallbacks_ ? X.asyncExecute(l) : this.onSettledCallbacks_.push(l);
    };
    h.resolve = v;
    h.reject = function (e) {
      return new h(function (f, h) {
        h(e);
      });
    };
    h.race = function (e) {
      return new h(function (f, h) {
        for (var l = $jscomp.makeIterator(e), m = l.next(); !m.done; m = l.next())
          v(m.value).callWhenSettled_(f, h);
      });
    };
    h.all = function (e) {
      var f = $jscomp.makeIterator(e),
        m = f.next();
      return m.done
        ? v([])
        : new h(function (e, h) {
            function l(f) {
              return function (h) {
                t[f] = h;
                z--;
                0 == z && e(t);
              };
            }
            var t = [],
              z = 0;
            do t.push(void 0), z++, v(m.value).callWhenSettled_(l(t.length - 1), h), (m = f.next());
            while (!m.done);
          });
    };
    return h;
  },
  "es6",
  "es3"
);
var DracoDecoderModule = (function () {
  var f =
    "undefined" !== typeof document && document.currentScript ? document.currentScript.src : void 0;
  "undefined" !== typeof __filename && (f = f || __filename);
  return function (m) {
    function v(k) {
      return a.locateFile ? a.locateFile(k, M) : M + k;
    }
    function t(a, c) {
      a || z("Assertion failed: " + c);
    }
    function h(a, c, b) {
      var d = c + b;
      for (b = c; a[b] && !(b >= d); ) ++b;
      if (16 < b - c && a.subarray && xa) return xa.decode(a.subarray(c, b));
      for (d = ""; c < b; ) {
        var k = a[c++];
        if (k & 128) {
          var e = a[c++] & 63;
          if (192 == (k & 224)) d += String.fromCharCode(((k & 31) << 6) | e);
          else {
            var f = a[c++] & 63;
            k =
              224 == (k & 240)
                ? ((k & 15) << 12) | (e << 6) | f
                : ((k & 7) << 18) | (e << 12) | (f << 6) | (a[c++] & 63);
            65536 > k
              ? (d += String.fromCharCode(k))
              : ((k -= 65536), (d += String.fromCharCode(55296 | (k >> 10), 56320 | (k & 1023))));
          }
        } else d += String.fromCharCode(k);
      }
      return d;
    }
    function X(a, c) {
      return a ? h(ca, a, c) : "";
    }
    function e(a, c) {
      0 < a % c && (a += c - (a % c));
      return a;
    }
    function l(k) {
      ka = k;
      a.HEAP8 = T = new Int8Array(k);
      a.HEAP16 = new Int16Array(k);
      a.HEAP32 = P = new Int32Array(k);
      a.HEAPU8 = ca = new Uint8Array(k);
      a.HEAPU16 = new Uint16Array(k);
      a.HEAPU32 = new Uint32Array(k);
      a.HEAPF32 = new Float32Array(k);
      a.HEAPF64 = new Float64Array(k);
    }
    function S(k) {
      for (; 0 < k.length; ) {
        var c = k.shift();
        if ("function" == typeof c) c();
        else {
          var b = c.func;
          "number" === typeof b
            ? void 0 === c.arg
              ? a.dynCall_v(b)
              : a.dynCall_vi(b, c.arg)
            : b(void 0 === c.arg ? null : c.arg);
        }
      }
    }
    function z(k) {
      if (a.onAbort) a.onAbort(k);
      k += "";
      ya(k);
      Y(k);
      za = !0;
      throw new WebAssembly.RuntimeError(
        "abort(" + k + "). Build with -s ASSERTIONS=1 for more info."
      );
    }
    function va(a) {
      return String.prototype.startsWith
        ? a.startsWith("data:application/octet-stream;base64,")
        : 0 === a.indexOf("data:application/octet-stream;base64,");
    }
    function wa() {
      try {
        if (da) return new Uint8Array(da);
        if (la) return la(U);
        throw "both async and sync fetching of the wasm failed";
      } catch (k) {
        z(k);
      }
    }
    function Ma() {
      return da || (!ea && !Z) || "function" !== typeof fetch
        ? new Promise(function (a, c) {
            a(wa());
          })
        : fetch(U, { credentials: "same-origin" })
            .then(function (a) {
              if (!a.ok) throw "failed to load wasm binary file at '" + U + "'";
              return a.arrayBuffer();
            })
            .catch(function () {
              return wa();
            });
    }
    function ba() {
      if (!ba.strings) {
        var a = {
            USER: "web_user",
            LOGNAME: "web_user",
            PATH: "/",
            PWD: "/",
            HOME: "/home/web_user",
            LANG:
              (
                ("object" === typeof navigator && navigator.languages && navigator.languages[0]) ||
                "C"
              ).replace("-", "_") + ".UTF-8",
            _: na
          },
          c;
        for (c in Aa) a[c] = Aa[c];
        var b = [];
        for (c in a) b.push(c + "=" + a[c]);
        ba.strings = b;
      }
      return ba.strings;
    }
    function ma(k) {
      function c() {
        if (!fa && ((fa = !0), !za)) {
          Ba = !0;
          S(Ca);
          S(Da);
          if (a.onRuntimeInitialized) a.onRuntimeInitialized();
          if (a.postRun)
            for ("function" == typeof a.postRun && (a.postRun = [a.postRun]); a.postRun.length; )
              Ea.unshift(a.postRun.shift());
          S(Ea);
        }
      }
      if (!(0 < aa)) {
        if (a.preRun)
          for ("function" == typeof a.preRun && (a.preRun = [a.preRun]); a.preRun.length; )
            Fa.unshift(a.preRun.shift());
        S(Fa);
        0 < aa ||
          (a.setStatus
            ? (a.setStatus("Running..."),
              setTimeout(function () {
                setTimeout(function () {
                  a.setStatus("");
                }, 1);
                c();
              }, 1))
            : c());
      }
    }
    function p() {}
    function u(a) {
      return (a || p).__cache__;
    }
    function N(a, c) {
      var b = u(c),
        d = b[a];
      if (d) return d;
      d = Object.create((c || p).prototype);
      d.ptr = a;
      return (b[a] = d);
    }
    function V(a) {
      if ("string" === typeof a) {
        for (var c = 0, b = 0; b < a.length; ++b) {
          var d = a.charCodeAt(b);
          55296 <= d &&
            57343 >= d &&
            (d = (65536 + ((d & 1023) << 10)) | (a.charCodeAt(++b) & 1023));
          127 >= d ? ++c : (c = 2047 >= d ? c + 2 : 65535 >= d ? c + 3 : c + 4);
        }
        c = Array(c + 1);
        b = 0;
        d = c.length;
        if (0 < d) {
          d = b + d - 1;
          for (var k = 0; k < a.length; ++k) {
            var e = a.charCodeAt(k);
            if (55296 <= e && 57343 >= e) {
              var f = a.charCodeAt(++k);
              e = (65536 + ((e & 1023) << 10)) | (f & 1023);
            }
            if (127 >= e) {
              if (b >= d) break;
              c[b++] = e;
            } else {
              if (2047 >= e) {
                if (b + 1 >= d) break;
                c[b++] = 192 | (e >> 6);
              } else {
                if (65535 >= e) {
                  if (b + 2 >= d) break;
                  c[b++] = 224 | (e >> 12);
                } else {
                  if (b + 3 >= d) break;
                  c[b++] = 240 | (e >> 18);
                  c[b++] = 128 | ((e >> 12) & 63);
                }
                c[b++] = 128 | ((e >> 6) & 63);
              }
              c[b++] = 128 | (e & 63);
            }
          }
          c[b] = 0;
        }
        a = n.alloc(c, T);
        n.copy(c, T, a);
      }
      return a;
    }
    function x() {
      throw "cannot construct a Status, no constructor in IDL";
    }
    function A() {
      this.ptr = Oa();
      u(A)[this.ptr] = this;
    }
    function B() {
      this.ptr = Pa();
      u(B)[this.ptr] = this;
    }
    function C() {
      this.ptr = Qa();
      u(C)[this.ptr] = this;
    }
    function D() {
      this.ptr = Ra();
      u(D)[this.ptr] = this;
    }
    function E() {
      this.ptr = Sa();
      u(E)[this.ptr] = this;
    }
    function q() {
      this.ptr = Ta();
      u(q)[this.ptr] = this;
    }
    function J() {
      this.ptr = Ua();
      u(J)[this.ptr] = this;
    }
    function w() {
      this.ptr = Va();
      u(w)[this.ptr] = this;
    }
    function F() {
      this.ptr = Wa();
      u(F)[this.ptr] = this;
    }
    function r() {
      this.ptr = Xa();
      u(r)[this.ptr] = this;
    }
    function G() {
      this.ptr = Ya();
      u(G)[this.ptr] = this;
    }
    function H() {
      this.ptr = Za();
      u(H)[this.ptr] = this;
    }
    function O() {
      this.ptr = $a();
      u(O)[this.ptr] = this;
    }
    function K() {
      this.ptr = ab();
      u(K)[this.ptr] = this;
    }
    function g() {
      this.ptr = bb();
      u(g)[this.ptr] = this;
    }
    function y() {
      this.ptr = cb();
      u(y)[this.ptr] = this;
    }
    function Q() {
      throw "cannot construct a VoidPtr, no constructor in IDL";
    }
    function I() {
      this.ptr = db();
      u(I)[this.ptr] = this;
    }
    function L() {
      this.ptr = eb();
      u(L)[this.ptr] = this;
    }
    m = m || {};
    var a = "undefined" !== typeof m ? m : {},
      Ga = !1,
      Ha = !1;
    a.onRuntimeInitialized = function () {
      Ga = !0;
      if (Ha && "function" === typeof a.onModuleLoaded) a.onModuleLoaded(a);
    };
    a.onModuleParsed = function () {
      Ha = !0;
      if (Ga && "function" === typeof a.onModuleLoaded) a.onModuleLoaded(a);
    };
    a.isVersionSupported = function (a) {
      if ("string" !== typeof a) return !1;
      a = a.split(".");
      return 2 > a.length || 3 < a.length
        ? !1
        : 1 == a[0] && 0 <= a[1] && 3 >= a[1]
        ? !0
        : 0 != a[0] || 10 < a[1]
        ? !1
        : !0;
    };
    var ha = {},
      W;
    for (W in a) a.hasOwnProperty(W) && (ha[W] = a[W]);
    var na = "./this.program",
      ea = !1,
      Z = !1,
      oa = !1,
      fb = !1,
      Ia = !1;
    ea = "object" === typeof window;
    Z = "function" === typeof importScripts;
    oa =
      (fb =
        "object" === typeof process &&
        "object" === typeof process.versions &&
        "string" === typeof process.versions.node) &&
      !ea &&
      !Z;
    Ia = !ea && !oa && !Z;
    var M = "",
      pa,
      qa;
    if (oa) {
      M = __dirname + "/";
      var ra = function (a, c) {
        pa || (pa = require("fs"));
        qa || (qa = require("path"));
        a = qa.normalize(a);
        return pa.readFileSync(a, c ? null : "utf8");
      };
      var la = function (a) {
        a = ra(a, !0);
        a.buffer || (a = new Uint8Array(a));
        t(a.buffer);
        return a;
      };
      1 < process.argv.length && (na = process.argv[1].replace(/\\/g, "/"));
      process.argv.slice(2);
      process.on("uncaughtException", function (a) {
        throw a;
      });
      process.on("unhandledRejection", z);
      a.inspect = function () {
        return "[Emscripten Module object]";
      };
    } else if (Ia)
      "undefined" != typeof read &&
        (ra = function (a) {
          return read(a);
        }),
        (la = function (a) {
          if ("function" === typeof readbuffer) return new Uint8Array(readbuffer(a));
          a = read(a, "binary");
          t("object" === typeof a);
          return a;
        }),
        "undefined" !== typeof print &&
          ("undefined" === typeof console && (console = {}),
          (console.log = print),
          (console.warn = console.error = "undefined" !== typeof printErr ? printErr : print));
    else if (ea || Z)
      Z ? (M = self.location.href) : document.currentScript && (M = document.currentScript.src),
        f && (M = f),
        (M = 0 !== M.indexOf("blob:") ? M.substr(0, M.lastIndexOf("/") + 1) : ""),
        (ra = function (a) {
          var c = new XMLHttpRequest();
          c.open("GET", a, !1);
          c.send(null);
          return c.responseText;
        }),
        Z &&
          (la = function (a) {
            var c = new XMLHttpRequest();
            c.open("GET", a, !1);
            c.responseType = "arraybuffer";
            c.send(null);
            return new Uint8Array(c.response);
          });
    var ya = a.print || console.log.bind(console),
      Y = a.printErr || console.warn.bind(console);
    for (W in ha) ha.hasOwnProperty(W) && (a[W] = ha[W]);
    ha = null;
    a.thisProgram && (na = a.thisProgram);
    var da;
    a.wasmBinary && (da = a.wasmBinary);
    "object" !== typeof WebAssembly && Y("no native wasm support detected");
    var ia,
      gb = new WebAssembly.Table({ initial: 293, maximum: 293, element: "anyfunc" }),
      za = !1,
      xa = "undefined" !== typeof TextDecoder ? new TextDecoder("utf8") : void 0;
    "undefined" !== typeof TextDecoder && new TextDecoder("utf-16le");
    var T,
      ca,
      P,
      Ja = a.TOTAL_MEMORY || 16777216;
    if ((ia = a.wasmMemory ? a.wasmMemory : new WebAssembly.Memory({ initial: Ja / 65536 })))
      var ka = ia.buffer;
    Ja = ka.byteLength;
    l(ka);
    P[3416] = 5256704;
    var Fa = [],
      Ca = [],
      Da = [],
      Ea = [],
      Ba = !1,
      aa = 0,
      sa = null,
      ja = null;
    a.preloadedImages = {};
    a.preloadedAudios = {};
    var U = "draco_decoder.wasm";
    va(U) || (U = v(U));
    Ca.push({
      func: function () {
        hb();
      }
    });
    var Aa = {},
      R = {
        buffers: [null, [], []],
        printChar: function (a, c) {
          var b = R.buffers[a];
          0 === c || 10 === c ? ((1 === a ? ya : Y)(h(b, 0)), (b.length = 0)) : b.push(c);
        },
        varargs: 0,
        get: function (a) {
          R.varargs += 4;
          return P[(R.varargs - 4) >> 2];
        },
        getStr: function () {
          return X(R.get());
        },
        get64: function () {
          var a = R.get();
          R.get();
          return a;
        },
        getZero: function () {
          R.get();
        }
      },
      Ka = {
        __cxa_allocate_exception: function (a) {
          return ib(a);
        },
        __cxa_throw: function (a, c, b) {
          "uncaught_exception" in ta ? ta.uncaught_exceptions++ : (ta.uncaught_exceptions = 1);
          throw a;
        },
        abort: function () {
          z();
        },
        emscripten_get_sbrk_ptr: function () {
          return 13664;
        },
        emscripten_memcpy_big: function (a, c, b) {
          ca.set(ca.subarray(c, c + b), a);
        },
        emscripten_resize_heap: function (a) {
          if (2147418112 < a) return !1;
          for (var c = Math.max(T.length, 16777216); c < a; )
            c =
              536870912 >= c
                ? e(2 * c, 65536)
                : Math.min(e((3 * c + 2147483648) / 4, 65536), 2147418112);
          a: {
            try {
              ia.grow((c - ka.byteLength + 65535) >> 16);
              l(ia.buffer);
              var b = 1;
              break a;
            } catch (d) {}
            b = void 0;
          }
          return b ? !0 : !1;
        },
        environ_get: function (a, c) {
          var b = 0;
          ba().forEach(function (d, e) {
            var f = c + b;
            e = P[(a + 4 * e) >> 2] = f;
            for (f = 0; f < d.length; ++f) T[e++ >> 0] = d.charCodeAt(f);
            T[e >> 0] = 0;
            b += d.length + 1;
          });
          return 0;
        },
        environ_sizes_get: function (a, c) {
          var b = ba();
          P[a >> 2] = b.length;
          var d = 0;
          b.forEach(function (a) {
            d += a.length + 1;
          });
          P[c >> 2] = d;
          return 0;
        },
        fd_close: function (a) {
          return 0;
        },
        fd_seek: function (a, c, b, d, e) {
          return 0;
        },
        fd_write: function (a, c, b, d) {
          try {
            for (var e = 0, f = 0; f < b; f++) {
              for (var g = P[(c + 8 * f) >> 2], k = P[(c + (8 * f + 4)) >> 2], h = 0; h < k; h++)
                R.printChar(a, ca[g + h]);
              e += k;
            }
            P[d >> 2] = e;
            return 0;
          } catch (ua) {
            return ("undefined" !== typeof FS && ua instanceof FS.ErrnoError) || z(ua), ua.errno;
          }
        },
        memory: ia,
        setTempRet0: function (a) {},
        table: gb
      },
      La = (function () {
        function e(c, b) {
          a.asm = c.exports;
          aa--;
          a.monitorRunDependencies && a.monitorRunDependencies(aa);
          0 == aa &&
            (null !== sa && (clearInterval(sa), (sa = null)), ja && ((c = ja), (ja = null), c()));
        }
        function c(a) {
          e(a.instance);
        }
        function b(a) {
          return Ma()
            .then(function (a) {
              return WebAssembly.instantiate(a, d);
            })
            .then(a, function (a) {
              Y("failed to asynchronously prepare wasm: " + a);
              z(a);
            });
        }
        var d = { env: Ka, wasi_unstable: Ka };
        aa++;
        a.monitorRunDependencies && a.monitorRunDependencies(aa);
        if (a.instantiateWasm)
          try {
            return a.instantiateWasm(d, e);
          } catch (Na) {
            return Y("Module.instantiateWasm callback failed with error: " + Na), !1;
          }
        (function () {
          if (
            da ||
            "function" !== typeof WebAssembly.instantiateStreaming ||
            va(U) ||
            "function" !== typeof fetch
          )
            return b(c);
          fetch(U, { credentials: "same-origin" }).then(function (a) {
            return WebAssembly.instantiateStreaming(a, d).then(c, function (a) {
              Y("wasm streaming compile failed: " + a);
              Y("falling back to ArrayBuffer instantiation");
              b(c);
            });
          });
        })();
        return {};
      })();
    a.asm = La;
    var hb = (a.___wasm_call_ctors = function () {
        return a.asm.__wasm_call_ctors.apply(null, arguments);
      }),
      jb = (a._emscripten_bind_Status_code_0 = function () {
        return a.asm.emscripten_bind_Status_code_0.apply(null, arguments);
      }),
      kb = (a._emscripten_bind_Status_ok_0 = function () {
        return a.asm.emscripten_bind_Status_ok_0.apply(null, arguments);
      }),
      lb = (a._emscripten_bind_Status_error_msg_0 = function () {
        return a.asm.emscripten_bind_Status_error_msg_0.apply(null, arguments);
      }),
      mb = (a._emscripten_bind_Status___destroy___0 = function () {
        return a.asm.emscripten_bind_Status___destroy___0.apply(null, arguments);
      }),
      Oa = (a._emscripten_bind_DracoUInt16Array_DracoUInt16Array_0 = function () {
        return a.asm.emscripten_bind_DracoUInt16Array_DracoUInt16Array_0.apply(null, arguments);
      }),
      nb = (a._emscripten_bind_DracoUInt16Array_GetValue_1 = function () {
        return a.asm.emscripten_bind_DracoUInt16Array_GetValue_1.apply(null, arguments);
      }),
      ob = (a._emscripten_bind_DracoUInt16Array_size_0 = function () {
        return a.asm.emscripten_bind_DracoUInt16Array_size_0.apply(null, arguments);
      }),
      pb = (a._emscripten_bind_DracoUInt16Array___destroy___0 = function () {
        return a.asm.emscripten_bind_DracoUInt16Array___destroy___0.apply(null, arguments);
      }),
      Pa = (a._emscripten_bind_PointCloud_PointCloud_0 = function () {
        return a.asm.emscripten_bind_PointCloud_PointCloud_0.apply(null, arguments);
      }),
      qb = (a._emscripten_bind_PointCloud_num_attributes_0 = function () {
        return a.asm.emscripten_bind_PointCloud_num_attributes_0.apply(null, arguments);
      }),
      rb = (a._emscripten_bind_PointCloud_num_points_0 = function () {
        return a.asm.emscripten_bind_PointCloud_num_points_0.apply(null, arguments);
      }),
      sb = (a._emscripten_bind_PointCloud___destroy___0 = function () {
        return a.asm.emscripten_bind_PointCloud___destroy___0.apply(null, arguments);
      }),
      Qa = (a._emscripten_bind_DracoUInt8Array_DracoUInt8Array_0 = function () {
        return a.asm.emscripten_bind_DracoUInt8Array_DracoUInt8Array_0.apply(null, arguments);
      }),
      tb = (a._emscripten_bind_DracoUInt8Array_GetValue_1 = function () {
        return a.asm.emscripten_bind_DracoUInt8Array_GetValue_1.apply(null, arguments);
      }),
      ub = (a._emscripten_bind_DracoUInt8Array_size_0 = function () {
        return a.asm.emscripten_bind_DracoUInt8Array_size_0.apply(null, arguments);
      }),
      vb = (a._emscripten_bind_DracoUInt8Array___destroy___0 = function () {
        return a.asm.emscripten_bind_DracoUInt8Array___destroy___0.apply(null, arguments);
      }),
      Ra = (a._emscripten_bind_DracoUInt32Array_DracoUInt32Array_0 = function () {
        return a.asm.emscripten_bind_DracoUInt32Array_DracoUInt32Array_0.apply(null, arguments);
      }),
      wb = (a._emscripten_bind_DracoUInt32Array_GetValue_1 = function () {
        return a.asm.emscripten_bind_DracoUInt32Array_GetValue_1.apply(null, arguments);
      }),
      xb = (a._emscripten_bind_DracoUInt32Array_size_0 = function () {
        return a.asm.emscripten_bind_DracoUInt32Array_size_0.apply(null, arguments);
      }),
      yb = (a._emscripten_bind_DracoUInt32Array___destroy___0 = function () {
        return a.asm.emscripten_bind_DracoUInt32Array___destroy___0.apply(null, arguments);
      }),
      Sa = (a._emscripten_bind_AttributeOctahedronTransform_AttributeOctahedronTransform_0 =
        function () {
          return a.asm.emscripten_bind_AttributeOctahedronTransform_AttributeOctahedronTransform_0.apply(
            null,
            arguments
          );
        }),
      zb = (a._emscripten_bind_AttributeOctahedronTransform_InitFromAttribute_1 = function () {
        return a.asm.emscripten_bind_AttributeOctahedronTransform_InitFromAttribute_1.apply(
          null,
          arguments
        );
      }),
      Ab = (a._emscripten_bind_AttributeOctahedronTransform_quantization_bits_0 = function () {
        return a.asm.emscripten_bind_AttributeOctahedronTransform_quantization_bits_0.apply(
          null,
          arguments
        );
      }),
      Bb = (a._emscripten_bind_AttributeOctahedronTransform___destroy___0 = function () {
        return a.asm.emscripten_bind_AttributeOctahedronTransform___destroy___0.apply(
          null,
          arguments
        );
      }),
      Ta = (a._emscripten_bind_PointAttribute_PointAttribute_0 = function () {
        return a.asm.emscripten_bind_PointAttribute_PointAttribute_0.apply(null, arguments);
      }),
      Cb = (a._emscripten_bind_PointAttribute_size_0 = function () {
        return a.asm.emscripten_bind_PointAttribute_size_0.apply(null, arguments);
      }),
      Db = (a._emscripten_bind_PointAttribute_GetAttributeTransformData_0 = function () {
        return a.asm.emscripten_bind_PointAttribute_GetAttributeTransformData_0.apply(
          null,
          arguments
        );
      }),
      Eb = (a._emscripten_bind_PointAttribute_attribute_type_0 = function () {
        return a.asm.emscripten_bind_PointAttribute_attribute_type_0.apply(null, arguments);
      }),
      Fb = (a._emscripten_bind_PointAttribute_data_type_0 = function () {
        return a.asm.emscripten_bind_PointAttribute_data_type_0.apply(null, arguments);
      }),
      Gb = (a._emscripten_bind_PointAttribute_num_components_0 = function () {
        return a.asm.emscripten_bind_PointAttribute_num_components_0.apply(null, arguments);
      }),
      Hb = (a._emscripten_bind_PointAttribute_normalized_0 = function () {
        return a.asm.emscripten_bind_PointAttribute_normalized_0.apply(null, arguments);
      }),
      Ib = (a._emscripten_bind_PointAttribute_byte_stride_0 = function () {
        return a.asm.emscripten_bind_PointAttribute_byte_stride_0.apply(null, arguments);
      }),
      Jb = (a._emscripten_bind_PointAttribute_byte_offset_0 = function () {
        return a.asm.emscripten_bind_PointAttribute_byte_offset_0.apply(null, arguments);
      }),
      Kb = (a._emscripten_bind_PointAttribute_unique_id_0 = function () {
        return a.asm.emscripten_bind_PointAttribute_unique_id_0.apply(null, arguments);
      }),
      Lb = (a._emscripten_bind_PointAttribute___destroy___0 = function () {
        return a.asm.emscripten_bind_PointAttribute___destroy___0.apply(null, arguments);
      }),
      Ua = (a._emscripten_bind_AttributeTransformData_AttributeTransformData_0 = function () {
        return a.asm.emscripten_bind_AttributeTransformData_AttributeTransformData_0.apply(
          null,
          arguments
        );
      }),
      Mb = (a._emscripten_bind_AttributeTransformData_transform_type_0 = function () {
        return a.asm.emscripten_bind_AttributeTransformData_transform_type_0.apply(null, arguments);
      }),
      Nb = (a._emscripten_bind_AttributeTransformData___destroy___0 = function () {
        return a.asm.emscripten_bind_AttributeTransformData___destroy___0.apply(null, arguments);
      }),
      Va = (a._emscripten_bind_AttributeQuantizationTransform_AttributeQuantizationTransform_0 =
        function () {
          return a.asm.emscripten_bind_AttributeQuantizationTransform_AttributeQuantizationTransform_0.apply(
            null,
            arguments
          );
        }),
      Ob = (a._emscripten_bind_AttributeQuantizationTransform_InitFromAttribute_1 = function () {
        return a.asm.emscripten_bind_AttributeQuantizationTransform_InitFromAttribute_1.apply(
          null,
          arguments
        );
      }),
      Pb = (a._emscripten_bind_AttributeQuantizationTransform_quantization_bits_0 = function () {
        return a.asm.emscripten_bind_AttributeQuantizationTransform_quantization_bits_0.apply(
          null,
          arguments
        );
      }),
      Qb = (a._emscripten_bind_AttributeQuantizationTransform_min_value_1 = function () {
        return a.asm.emscripten_bind_AttributeQuantizationTransform_min_value_1.apply(
          null,
          arguments
        );
      }),
      Rb = (a._emscripten_bind_AttributeQuantizationTransform_range_0 = function () {
        return a.asm.emscripten_bind_AttributeQuantizationTransform_range_0.apply(null, arguments);
      }),
      Sb = (a._emscripten_bind_AttributeQuantizationTransform___destroy___0 = function () {
        return a.asm.emscripten_bind_AttributeQuantizationTransform___destroy___0.apply(
          null,
          arguments
        );
      }),
      Wa = (a._emscripten_bind_DracoInt8Array_DracoInt8Array_0 = function () {
        return a.asm.emscripten_bind_DracoInt8Array_DracoInt8Array_0.apply(null, arguments);
      }),
      Tb = (a._emscripten_bind_DracoInt8Array_GetValue_1 = function () {
        return a.asm.emscripten_bind_DracoInt8Array_GetValue_1.apply(null, arguments);
      }),
      Ub = (a._emscripten_bind_DracoInt8Array_size_0 = function () {
        return a.asm.emscripten_bind_DracoInt8Array_size_0.apply(null, arguments);
      }),
      Vb = (a._emscripten_bind_DracoInt8Array___destroy___0 = function () {
        return a.asm.emscripten_bind_DracoInt8Array___destroy___0.apply(null, arguments);
      }),
      Xa = (a._emscripten_bind_MetadataQuerier_MetadataQuerier_0 = function () {
        return a.asm.emscripten_bind_MetadataQuerier_MetadataQuerier_0.apply(null, arguments);
      }),
      Wb = (a._emscripten_bind_MetadataQuerier_HasEntry_2 = function () {
        return a.asm.emscripten_bind_MetadataQuerier_HasEntry_2.apply(null, arguments);
      }),
      Xb = (a._emscripten_bind_MetadataQuerier_GetIntEntry_2 = function () {
        return a.asm.emscripten_bind_MetadataQuerier_GetIntEntry_2.apply(null, arguments);
      }),
      Yb = (a._emscripten_bind_MetadataQuerier_GetIntEntryArray_3 = function () {
        return a.asm.emscripten_bind_MetadataQuerier_GetIntEntryArray_3.apply(null, arguments);
      }),
      Zb = (a._emscripten_bind_MetadataQuerier_GetDoubleEntry_2 = function () {
        return a.asm.emscripten_bind_MetadataQuerier_GetDoubleEntry_2.apply(null, arguments);
      }),
      $b = (a._emscripten_bind_MetadataQuerier_GetStringEntry_2 = function () {
        return a.asm.emscripten_bind_MetadataQuerier_GetStringEntry_2.apply(null, arguments);
      }),
      ac = (a._emscripten_bind_MetadataQuerier_NumEntries_1 = function () {
        return a.asm.emscripten_bind_MetadataQuerier_NumEntries_1.apply(null, arguments);
      }),
      bc = (a._emscripten_bind_MetadataQuerier_GetEntryName_2 = function () {
        return a.asm.emscripten_bind_MetadataQuerier_GetEntryName_2.apply(null, arguments);
      }),
      cc = (a._emscripten_bind_MetadataQuerier___destroy___0 = function () {
        return a.asm.emscripten_bind_MetadataQuerier___destroy___0.apply(null, arguments);
      }),
      Ya = (a._emscripten_bind_DracoInt16Array_DracoInt16Array_0 = function () {
        return a.asm.emscripten_bind_DracoInt16Array_DracoInt16Array_0.apply(null, arguments);
      }),
      dc = (a._emscripten_bind_DracoInt16Array_GetValue_1 = function () {
        return a.asm.emscripten_bind_DracoInt16Array_GetValue_1.apply(null, arguments);
      }),
      ec = (a._emscripten_bind_DracoInt16Array_size_0 = function () {
        return a.asm.emscripten_bind_DracoInt16Array_size_0.apply(null, arguments);
      }),
      fc = (a._emscripten_bind_DracoInt16Array___destroy___0 = function () {
        return a.asm.emscripten_bind_DracoInt16Array___destroy___0.apply(null, arguments);
      }),
      Za = (a._emscripten_bind_DracoFloat32Array_DracoFloat32Array_0 = function () {
        return a.asm.emscripten_bind_DracoFloat32Array_DracoFloat32Array_0.apply(null, arguments);
      }),
      gc = (a._emscripten_bind_DracoFloat32Array_GetValue_1 = function () {
        return a.asm.emscripten_bind_DracoFloat32Array_GetValue_1.apply(null, arguments);
      }),
      hc = (a._emscripten_bind_DracoFloat32Array_size_0 = function () {
        return a.asm.emscripten_bind_DracoFloat32Array_size_0.apply(null, arguments);
      }),
      ic = (a._emscripten_bind_DracoFloat32Array___destroy___0 = function () {
        return a.asm.emscripten_bind_DracoFloat32Array___destroy___0.apply(null, arguments);
      }),
      $a = (a._emscripten_bind_GeometryAttribute_GeometryAttribute_0 = function () {
        return a.asm.emscripten_bind_GeometryAttribute_GeometryAttribute_0.apply(null, arguments);
      }),
      jc = (a._emscripten_bind_GeometryAttribute___destroy___0 = function () {
        return a.asm.emscripten_bind_GeometryAttribute___destroy___0.apply(null, arguments);
      }),
      ab = (a._emscripten_bind_DecoderBuffer_DecoderBuffer_0 = function () {
        return a.asm.emscripten_bind_DecoderBuffer_DecoderBuffer_0.apply(null, arguments);
      }),
      kc = (a._emscripten_bind_DecoderBuffer_Init_2 = function () {
        return a.asm.emscripten_bind_DecoderBuffer_Init_2.apply(null, arguments);
      }),
      lc = (a._emscripten_bind_DecoderBuffer___destroy___0 = function () {
        return a.asm.emscripten_bind_DecoderBuffer___destroy___0.apply(null, arguments);
      }),
      bb = (a._emscripten_bind_Decoder_Decoder_0 = function () {
        return a.asm.emscripten_bind_Decoder_Decoder_0.apply(null, arguments);
      }),
      mc = (a._emscripten_bind_Decoder_GetEncodedGeometryType_1 = function () {
        return a.asm.emscripten_bind_Decoder_GetEncodedGeometryType_1.apply(null, arguments);
      }),
      nc = (a._emscripten_bind_Decoder_DecodeBufferToPointCloud_2 = function () {
        return a.asm.emscripten_bind_Decoder_DecodeBufferToPointCloud_2.apply(null, arguments);
      }),
      oc = (a._emscripten_bind_Decoder_DecodeBufferToMesh_2 = function () {
        return a.asm.emscripten_bind_Decoder_DecodeBufferToMesh_2.apply(null, arguments);
      }),
      pc = (a._emscripten_bind_Decoder_GetAttributeId_2 = function () {
        return a.asm.emscripten_bind_Decoder_GetAttributeId_2.apply(null, arguments);
      }),
      qc = (a._emscripten_bind_Decoder_GetAttributeIdByName_2 = function () {
        return a.asm.emscripten_bind_Decoder_GetAttributeIdByName_2.apply(null, arguments);
      }),
      rc = (a._emscripten_bind_Decoder_GetAttributeIdByMetadataEntry_3 = function () {
        return a.asm.emscripten_bind_Decoder_GetAttributeIdByMetadataEntry_3.apply(null, arguments);
      }),
      sc = (a._emscripten_bind_Decoder_GetAttribute_2 = function () {
        return a.asm.emscripten_bind_Decoder_GetAttribute_2.apply(null, arguments);
      }),
      tc = (a._emscripten_bind_Decoder_GetAttributeByUniqueId_2 = function () {
        return a.asm.emscripten_bind_Decoder_GetAttributeByUniqueId_2.apply(null, arguments);
      }),
      uc = (a._emscripten_bind_Decoder_GetMetadata_1 = function () {
        return a.asm.emscripten_bind_Decoder_GetMetadata_1.apply(null, arguments);
      }),
      vc = (a._emscripten_bind_Decoder_GetAttributeMetadata_2 = function () {
        return a.asm.emscripten_bind_Decoder_GetAttributeMetadata_2.apply(null, arguments);
      }),
      wc = (a._emscripten_bind_Decoder_GetFaceFromMesh_3 = function () {
        return a.asm.emscripten_bind_Decoder_GetFaceFromMesh_3.apply(null, arguments);
      }),
      xc = (a._emscripten_bind_Decoder_GetTriangleStripsFromMesh_2 = function () {
        return a.asm.emscripten_bind_Decoder_GetTriangleStripsFromMesh_2.apply(null, arguments);
      }),
      yc = (a._emscripten_bind_Decoder_GetTrianglesUInt16Array_3 = function () {
        return a.asm.emscripten_bind_Decoder_GetTrianglesUInt16Array_3.apply(null, arguments);
      }),
      zc = (a._emscripten_bind_Decoder_GetTrianglesUInt32Array_3 = function () {
        return a.asm.emscripten_bind_Decoder_GetTrianglesUInt32Array_3.apply(null, arguments);
      }),
      Ac = (a._emscripten_bind_Decoder_GetAttributeFloat_3 = function () {
        return a.asm.emscripten_bind_Decoder_GetAttributeFloat_3.apply(null, arguments);
      }),
      Bc = (a._emscripten_bind_Decoder_GetAttributeFloatForAllPoints_3 = function () {
        return a.asm.emscripten_bind_Decoder_GetAttributeFloatForAllPoints_3.apply(null, arguments);
      }),
      Cc = (a._emscripten_bind_Decoder_GetAttributeIntForAllPoints_3 = function () {
        return a.asm.emscripten_bind_Decoder_GetAttributeIntForAllPoints_3.apply(null, arguments);
      }),
      Dc = (a._emscripten_bind_Decoder_GetAttributeInt8ForAllPoints_3 = function () {
        return a.asm.emscripten_bind_Decoder_GetAttributeInt8ForAllPoints_3.apply(null, arguments);
      }),
      Ec = (a._emscripten_bind_Decoder_GetAttributeUInt8ForAllPoints_3 = function () {
        return a.asm.emscripten_bind_Decoder_GetAttributeUInt8ForAllPoints_3.apply(null, arguments);
      }),
      Fc = (a._emscripten_bind_Decoder_GetAttributeInt16ForAllPoints_3 = function () {
        return a.asm.emscripten_bind_Decoder_GetAttributeInt16ForAllPoints_3.apply(null, arguments);
      }),
      Gc = (a._emscripten_bind_Decoder_GetAttributeUInt16ForAllPoints_3 = function () {
        return a.asm.emscripten_bind_Decoder_GetAttributeUInt16ForAllPoints_3.apply(
          null,
          arguments
        );
      }),
      Hc = (a._emscripten_bind_Decoder_GetAttributeInt32ForAllPoints_3 = function () {
        return a.asm.emscripten_bind_Decoder_GetAttributeInt32ForAllPoints_3.apply(null, arguments);
      }),
      Ic = (a._emscripten_bind_Decoder_GetAttributeUInt32ForAllPoints_3 = function () {
        return a.asm.emscripten_bind_Decoder_GetAttributeUInt32ForAllPoints_3.apply(
          null,
          arguments
        );
      }),
      Jc = (a._emscripten_bind_Decoder_GetAttributeDataArrayForAllPoints_5 = function () {
        return a.asm.emscripten_bind_Decoder_GetAttributeDataArrayForAllPoints_5.apply(
          null,
          arguments
        );
      }),
      Kc = (a._emscripten_bind_Decoder_SkipAttributeTransform_1 = function () {
        return a.asm.emscripten_bind_Decoder_SkipAttributeTransform_1.apply(null, arguments);
      }),
      Lc = (a._emscripten_bind_Decoder___destroy___0 = function () {
        return a.asm.emscripten_bind_Decoder___destroy___0.apply(null, arguments);
      }),
      cb = (a._emscripten_bind_Mesh_Mesh_0 = function () {
        return a.asm.emscripten_bind_Mesh_Mesh_0.apply(null, arguments);
      }),
      Mc = (a._emscripten_bind_Mesh_num_faces_0 = function () {
        return a.asm.emscripten_bind_Mesh_num_faces_0.apply(null, arguments);
      }),
      Nc = (a._emscripten_bind_Mesh_num_attributes_0 = function () {
        return a.asm.emscripten_bind_Mesh_num_attributes_0.apply(null, arguments);
      }),
      Oc = (a._emscripten_bind_Mesh_num_points_0 = function () {
        return a.asm.emscripten_bind_Mesh_num_points_0.apply(null, arguments);
      }),
      Pc = (a._emscripten_bind_Mesh___destroy___0 = function () {
        return a.asm.emscripten_bind_Mesh___destroy___0.apply(null, arguments);
      }),
      Qc = (a._emscripten_bind_VoidPtr___destroy___0 = function () {
        return a.asm.emscripten_bind_VoidPtr___destroy___0.apply(null, arguments);
      }),
      db = (a._emscripten_bind_DracoInt32Array_DracoInt32Array_0 = function () {
        return a.asm.emscripten_bind_DracoInt32Array_DracoInt32Array_0.apply(null, arguments);
      }),
      Rc = (a._emscripten_bind_DracoInt32Array_GetValue_1 = function () {
        return a.asm.emscripten_bind_DracoInt32Array_GetValue_1.apply(null, arguments);
      }),
      Sc = (a._emscripten_bind_DracoInt32Array_size_0 = function () {
        return a.asm.emscripten_bind_DracoInt32Array_size_0.apply(null, arguments);
      }),
      Tc = (a._emscripten_bind_DracoInt32Array___destroy___0 = function () {
        return a.asm.emscripten_bind_DracoInt32Array___destroy___0.apply(null, arguments);
      }),
      eb = (a._emscripten_bind_Metadata_Metadata_0 = function () {
        return a.asm.emscripten_bind_Metadata_Metadata_0.apply(null, arguments);
      }),
      Uc = (a._emscripten_bind_Metadata___destroy___0 = function () {
        return a.asm.emscripten_bind_Metadata___destroy___0.apply(null, arguments);
      }),
      Vc = (a._emscripten_enum_draco_StatusCode_OK = function () {
        return a.asm.emscripten_enum_draco_StatusCode_OK.apply(null, arguments);
      }),
      Wc = (a._emscripten_enum_draco_StatusCode_DRACO_ERROR = function () {
        return a.asm.emscripten_enum_draco_StatusCode_DRACO_ERROR.apply(null, arguments);
      }),
      Xc = (a._emscripten_enum_draco_StatusCode_IO_ERROR = function () {
        return a.asm.emscripten_enum_draco_StatusCode_IO_ERROR.apply(null, arguments);
      }),
      Yc = (a._emscripten_enum_draco_StatusCode_INVALID_PARAMETER = function () {
        return a.asm.emscripten_enum_draco_StatusCode_INVALID_PARAMETER.apply(null, arguments);
      }),
      Zc = (a._emscripten_enum_draco_StatusCode_UNSUPPORTED_VERSION = function () {
        return a.asm.emscripten_enum_draco_StatusCode_UNSUPPORTED_VERSION.apply(null, arguments);
      }),
      $c = (a._emscripten_enum_draco_StatusCode_UNKNOWN_VERSION = function () {
        return a.asm.emscripten_enum_draco_StatusCode_UNKNOWN_VERSION.apply(null, arguments);
      }),
      ad = (a._emscripten_enum_draco_DataType_DT_INVALID = function () {
        return a.asm.emscripten_enum_draco_DataType_DT_INVALID.apply(null, arguments);
      }),
      bd = (a._emscripten_enum_draco_DataType_DT_INT8 = function () {
        return a.asm.emscripten_enum_draco_DataType_DT_INT8.apply(null, arguments);
      }),
      cd = (a._emscripten_enum_draco_DataType_DT_UINT8 = function () {
        return a.asm.emscripten_enum_draco_DataType_DT_UINT8.apply(null, arguments);
      }),
      dd = (a._emscripten_enum_draco_DataType_DT_INT16 = function () {
        return a.asm.emscripten_enum_draco_DataType_DT_INT16.apply(null, arguments);
      }),
      ed = (a._emscripten_enum_draco_DataType_DT_UINT16 = function () {
        return a.asm.emscripten_enum_draco_DataType_DT_UINT16.apply(null, arguments);
      }),
      fd = (a._emscripten_enum_draco_DataType_DT_INT32 = function () {
        return a.asm.emscripten_enum_draco_DataType_DT_INT32.apply(null, arguments);
      }),
      gd = (a._emscripten_enum_draco_DataType_DT_UINT32 = function () {
        return a.asm.emscripten_enum_draco_DataType_DT_UINT32.apply(null, arguments);
      }),
      hd = (a._emscripten_enum_draco_DataType_DT_INT64 = function () {
        return a.asm.emscripten_enum_draco_DataType_DT_INT64.apply(null, arguments);
      }),
      id = (a._emscripten_enum_draco_DataType_DT_UINT64 = function () {
        return a.asm.emscripten_enum_draco_DataType_DT_UINT64.apply(null, arguments);
      }),
      jd = (a._emscripten_enum_draco_DataType_DT_FLOAT32 = function () {
        return a.asm.emscripten_enum_draco_DataType_DT_FLOAT32.apply(null, arguments);
      }),
      kd = (a._emscripten_enum_draco_DataType_DT_FLOAT64 = function () {
        return a.asm.emscripten_enum_draco_DataType_DT_FLOAT64.apply(null, arguments);
      }),
      ld = (a._emscripten_enum_draco_DataType_DT_BOOL = function () {
        return a.asm.emscripten_enum_draco_DataType_DT_BOOL.apply(null, arguments);
      }),
      md = (a._emscripten_enum_draco_DataType_DT_TYPES_COUNT = function () {
        return a.asm.emscripten_enum_draco_DataType_DT_TYPES_COUNT.apply(null, arguments);
      }),
      nd = (a._emscripten_enum_draco_EncodedGeometryType_INVALID_GEOMETRY_TYPE = function () {
        return a.asm.emscripten_enum_draco_EncodedGeometryType_INVALID_GEOMETRY_TYPE.apply(
          null,
          arguments
        );
      }),
      od = (a._emscripten_enum_draco_EncodedGeometryType_POINT_CLOUD = function () {
        return a.asm.emscripten_enum_draco_EncodedGeometryType_POINT_CLOUD.apply(null, arguments);
      }),
      pd = (a._emscripten_enum_draco_EncodedGeometryType_TRIANGULAR_MESH = function () {
        return a.asm.emscripten_enum_draco_EncodedGeometryType_TRIANGULAR_MESH.apply(
          null,
          arguments
        );
      }),
      qd = (a._emscripten_enum_draco_AttributeTransformType_ATTRIBUTE_INVALID_TRANSFORM =
        function () {
          return a.asm.emscripten_enum_draco_AttributeTransformType_ATTRIBUTE_INVALID_TRANSFORM.apply(
            null,
            arguments
          );
        }),
      rd = (a._emscripten_enum_draco_AttributeTransformType_ATTRIBUTE_NO_TRANSFORM = function () {
        return a.asm.emscripten_enum_draco_AttributeTransformType_ATTRIBUTE_NO_TRANSFORM.apply(
          null,
          arguments
        );
      }),
      sd = (a._emscripten_enum_draco_AttributeTransformType_ATTRIBUTE_QUANTIZATION_TRANSFORM =
        function () {
          return a.asm.emscripten_enum_draco_AttributeTransformType_ATTRIBUTE_QUANTIZATION_TRANSFORM.apply(
            null,
            arguments
          );
        }),
      td = (a._emscripten_enum_draco_AttributeTransformType_ATTRIBUTE_OCTAHEDRON_TRANSFORM =
        function () {
          return a.asm.emscripten_enum_draco_AttributeTransformType_ATTRIBUTE_OCTAHEDRON_TRANSFORM.apply(
            null,
            arguments
          );
        }),
      ud = (a._emscripten_enum_draco_GeometryAttribute_Type_INVALID = function () {
        return a.asm.emscripten_enum_draco_GeometryAttribute_Type_INVALID.apply(null, arguments);
      }),
      vd = (a._emscripten_enum_draco_GeometryAttribute_Type_POSITION = function () {
        return a.asm.emscripten_enum_draco_GeometryAttribute_Type_POSITION.apply(null, arguments);
      }),
      wd = (a._emscripten_enum_draco_GeometryAttribute_Type_NORMAL = function () {
        return a.asm.emscripten_enum_draco_GeometryAttribute_Type_NORMAL.apply(null, arguments);
      }),
      xd = (a._emscripten_enum_draco_GeometryAttribute_Type_COLOR = function () {
        return a.asm.emscripten_enum_draco_GeometryAttribute_Type_COLOR.apply(null, arguments);
      }),
      yd = (a._emscripten_enum_draco_GeometryAttribute_Type_TEX_COORD = function () {
        return a.asm.emscripten_enum_draco_GeometryAttribute_Type_TEX_COORD.apply(null, arguments);
      }),
      zd = (a._emscripten_enum_draco_GeometryAttribute_Type_GENERIC = function () {
        return a.asm.emscripten_enum_draco_GeometryAttribute_Type_GENERIC.apply(null, arguments);
      });
    a._setThrew = function () {
      return a.asm.setThrew.apply(null, arguments);
    };
    var ta = (a.__ZSt18uncaught_exceptionv = function () {
      return a.asm._ZSt18uncaught_exceptionv.apply(null, arguments);
    });
    a._free = function () {
      return a.asm.free.apply(null, arguments);
    };
    var ib = (a._malloc = function () {
      return a.asm.malloc.apply(null, arguments);
    });
    a.stackSave = function () {
      return a.asm.stackSave.apply(null, arguments);
    };
    a.stackAlloc = function () {
      return a.asm.stackAlloc.apply(null, arguments);
    };
    a.stackRestore = function () {
      return a.asm.stackRestore.apply(null, arguments);
    };
    a.__growWasmMemory = function () {
      return a.asm.__growWasmMemory.apply(null, arguments);
    };
    a.dynCall_ii = function () {
      return a.asm.dynCall_ii.apply(null, arguments);
    };
    a.dynCall_vi = function () {
      return a.asm.dynCall_vi.apply(null, arguments);
    };
    a.dynCall_iii = function () {
      return a.asm.dynCall_iii.apply(null, arguments);
    };
    a.dynCall_vii = function () {
      return a.asm.dynCall_vii.apply(null, arguments);
    };
    a.dynCall_iiii = function () {
      return a.asm.dynCall_iiii.apply(null, arguments);
    };
    a.dynCall_v = function () {
      return a.asm.dynCall_v.apply(null, arguments);
    };
    a.dynCall_viii = function () {
      return a.asm.dynCall_viii.apply(null, arguments);
    };
    a.dynCall_viiii = function () {
      return a.asm.dynCall_viiii.apply(null, arguments);
    };
    a.dynCall_iiiiiii = function () {
      return a.asm.dynCall_iiiiiii.apply(null, arguments);
    };
    a.dynCall_iidiiii = function () {
      return a.asm.dynCall_iidiiii.apply(null, arguments);
    };
    a.dynCall_jiji = function () {
      return a.asm.dynCall_jiji.apply(null, arguments);
    };
    a.dynCall_viiiiii = function () {
      return a.asm.dynCall_viiiiii.apply(null, arguments);
    };
    a.dynCall_viiiii = function () {
      return a.asm.dynCall_viiiii.apply(null, arguments);
    };
    a.asm = La;
    var fa;
    a.then = function (e) {
      if (fa) e(a);
      else {
        var c = a.onRuntimeInitialized;
        a.onRuntimeInitialized = function () {
          c && c();
          e(a);
        };
      }
      return a;
    };
    ja = function c() {
      fa || ma();
      fa || (ja = c);
    };
    a.run = ma;
    if (a.preInit)
      for ("function" == typeof a.preInit && (a.preInit = [a.preInit]); 0 < a.preInit.length; )
        a.preInit.pop()();
    ma();
    p.prototype = Object.create(p.prototype);
    p.prototype.constructor = p;
    p.prototype.__class__ = p;
    p.__cache__ = {};
    a.WrapperObject = p;
    a.getCache = u;
    a.wrapPointer = N;
    a.castObject = function (a, b) {
      return N(a.ptr, b);
    };
    a.NULL = N(0);
    a.destroy = function (a) {
      if (!a.__destroy__) throw "Error: Cannot destroy object. (Did you create it yourself?)";
      a.__destroy__();
      delete u(a.__class__)[a.ptr];
    };
    a.compare = function (a, b) {
      return a.ptr === b.ptr;
    };
    a.getPointer = function (a) {
      return a.ptr;
    };
    a.getClass = function (a) {
      return a.__class__;
    };
    var n = {
      buffer: 0,
      size: 0,
      pos: 0,
      temps: [],
      needed: 0,
      prepare: function () {
        if (n.needed) {
          for (var c = 0; c < n.temps.length; c++) a._free(n.temps[c]);
          n.temps.length = 0;
          a._free(n.buffer);
          n.buffer = 0;
          n.size += n.needed;
          n.needed = 0;
        }
        n.buffer || ((n.size += 128), (n.buffer = a._malloc(n.size)), t(n.buffer));
        n.pos = 0;
      },
      alloc: function (c, b) {
        t(n.buffer);
        c = c.length * b.BYTES_PER_ELEMENT;
        c = (c + 7) & -8;
        n.pos + c >= n.size
          ? (t(0 < c), (n.needed += c), (b = a._malloc(c)), n.temps.push(b))
          : ((b = n.buffer + n.pos), (n.pos += c));
        return b;
      },
      copy: function (a, b, d) {
        switch (b.BYTES_PER_ELEMENT) {
          case 2:
            d >>= 1;
            break;
          case 4:
            d >>= 2;
            break;
          case 8:
            d >>= 3;
        }
        for (var c = 0; c < a.length; c++) b[d + c] = a[c];
      }
    };
    x.prototype = Object.create(p.prototype);
    x.prototype.constructor = x;
    x.prototype.__class__ = x;
    x.__cache__ = {};
    a.Status = x;
    x.prototype.code = x.prototype.code = function () {
      return jb(this.ptr);
    };
    x.prototype.ok = x.prototype.ok = function () {
      return !!kb(this.ptr);
    };
    x.prototype.error_msg = x.prototype.error_msg = function () {
      return X(lb(this.ptr));
    };
    x.prototype.__destroy__ = x.prototype.__destroy__ = function () {
      mb(this.ptr);
    };
    A.prototype = Object.create(p.prototype);
    A.prototype.constructor = A;
    A.prototype.__class__ = A;
    A.__cache__ = {};
    a.DracoUInt16Array = A;
    A.prototype.GetValue = A.prototype.GetValue = function (a) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      return nb(c, a);
    };
    A.prototype.size = A.prototype.size = function () {
      return ob(this.ptr);
    };
    A.prototype.__destroy__ = A.prototype.__destroy__ = function () {
      pb(this.ptr);
    };
    B.prototype = Object.create(p.prototype);
    B.prototype.constructor = B;
    B.prototype.__class__ = B;
    B.__cache__ = {};
    a.PointCloud = B;
    B.prototype.num_attributes = B.prototype.num_attributes = function () {
      return qb(this.ptr);
    };
    B.prototype.num_points = B.prototype.num_points = function () {
      return rb(this.ptr);
    };
    B.prototype.__destroy__ = B.prototype.__destroy__ = function () {
      sb(this.ptr);
    };
    C.prototype = Object.create(p.prototype);
    C.prototype.constructor = C;
    C.prototype.__class__ = C;
    C.__cache__ = {};
    a.DracoUInt8Array = C;
    C.prototype.GetValue = C.prototype.GetValue = function (a) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      return tb(c, a);
    };
    C.prototype.size = C.prototype.size = function () {
      return ub(this.ptr);
    };
    C.prototype.__destroy__ = C.prototype.__destroy__ = function () {
      vb(this.ptr);
    };
    D.prototype = Object.create(p.prototype);
    D.prototype.constructor = D;
    D.prototype.__class__ = D;
    D.__cache__ = {};
    a.DracoUInt32Array = D;
    D.prototype.GetValue = D.prototype.GetValue = function (a) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      return wb(c, a);
    };
    D.prototype.size = D.prototype.size = function () {
      return xb(this.ptr);
    };
    D.prototype.__destroy__ = D.prototype.__destroy__ = function () {
      yb(this.ptr);
    };
    E.prototype = Object.create(p.prototype);
    E.prototype.constructor = E;
    E.prototype.__class__ = E;
    E.__cache__ = {};
    a.AttributeOctahedronTransform = E;
    E.prototype.InitFromAttribute = E.prototype.InitFromAttribute = function (a) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      return !!zb(c, a);
    };
    E.prototype.quantization_bits = E.prototype.quantization_bits = function () {
      return Ab(this.ptr);
    };
    E.prototype.__destroy__ = E.prototype.__destroy__ = function () {
      Bb(this.ptr);
    };
    q.prototype = Object.create(p.prototype);
    q.prototype.constructor = q;
    q.prototype.__class__ = q;
    q.__cache__ = {};
    a.PointAttribute = q;
    q.prototype.size = q.prototype.size = function () {
      return Cb(this.ptr);
    };
    q.prototype.GetAttributeTransformData = q.prototype.GetAttributeTransformData = function () {
      return N(Db(this.ptr), J);
    };
    q.prototype.attribute_type = q.prototype.attribute_type = function () {
      return Eb(this.ptr);
    };
    q.prototype.data_type = q.prototype.data_type = function () {
      return Fb(this.ptr);
    };
    q.prototype.num_components = q.prototype.num_components = function () {
      return Gb(this.ptr);
    };
    q.prototype.normalized = q.prototype.normalized = function () {
      return !!Hb(this.ptr);
    };
    q.prototype.byte_stride = q.prototype.byte_stride = function () {
      return Ib(this.ptr);
    };
    q.prototype.byte_offset = q.prototype.byte_offset = function () {
      return Jb(this.ptr);
    };
    q.prototype.unique_id = q.prototype.unique_id = function () {
      return Kb(this.ptr);
    };
    q.prototype.__destroy__ = q.prototype.__destroy__ = function () {
      Lb(this.ptr);
    };
    J.prototype = Object.create(p.prototype);
    J.prototype.constructor = J;
    J.prototype.__class__ = J;
    J.__cache__ = {};
    a.AttributeTransformData = J;
    J.prototype.transform_type = J.prototype.transform_type = function () {
      return Mb(this.ptr);
    };
    J.prototype.__destroy__ = J.prototype.__destroy__ = function () {
      Nb(this.ptr);
    };
    w.prototype = Object.create(p.prototype);
    w.prototype.constructor = w;
    w.prototype.__class__ = w;
    w.__cache__ = {};
    a.AttributeQuantizationTransform = w;
    w.prototype.InitFromAttribute = w.prototype.InitFromAttribute = function (a) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      return !!Ob(c, a);
    };
    w.prototype.quantization_bits = w.prototype.quantization_bits = function () {
      return Pb(this.ptr);
    };
    w.prototype.min_value = w.prototype.min_value = function (a) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      return Qb(c, a);
    };
    w.prototype.range = w.prototype.range = function () {
      return Rb(this.ptr);
    };
    w.prototype.__destroy__ = w.prototype.__destroy__ = function () {
      Sb(this.ptr);
    };
    F.prototype = Object.create(p.prototype);
    F.prototype.constructor = F;
    F.prototype.__class__ = F;
    F.__cache__ = {};
    a.DracoInt8Array = F;
    F.prototype.GetValue = F.prototype.GetValue = function (a) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      return Tb(c, a);
    };
    F.prototype.size = F.prototype.size = function () {
      return Ub(this.ptr);
    };
    F.prototype.__destroy__ = F.prototype.__destroy__ = function () {
      Vb(this.ptr);
    };
    r.prototype = Object.create(p.prototype);
    r.prototype.constructor = r;
    r.prototype.__class__ = r;
    r.__cache__ = {};
    a.MetadataQuerier = r;
    r.prototype.HasEntry = r.prototype.HasEntry = function (a, b) {
      var c = this.ptr;
      n.prepare();
      a && "object" === typeof a && (a = a.ptr);
      b = b && "object" === typeof b ? b.ptr : V(b);
      return !!Wb(c, a, b);
    };
    r.prototype.GetIntEntry = r.prototype.GetIntEntry = function (a, b) {
      var c = this.ptr;
      n.prepare();
      a && "object" === typeof a && (a = a.ptr);
      b = b && "object" === typeof b ? b.ptr : V(b);
      return Xb(c, a, b);
    };
    r.prototype.GetIntEntryArray = r.prototype.GetIntEntryArray = function (a, b, d) {
      var c = this.ptr;
      n.prepare();
      a && "object" === typeof a && (a = a.ptr);
      b = b && "object" === typeof b ? b.ptr : V(b);
      d && "object" === typeof d && (d = d.ptr);
      Yb(c, a, b, d);
    };
    r.prototype.GetDoubleEntry = r.prototype.GetDoubleEntry = function (a, b) {
      var c = this.ptr;
      n.prepare();
      a && "object" === typeof a && (a = a.ptr);
      b = b && "object" === typeof b ? b.ptr : V(b);
      return Zb(c, a, b);
    };
    r.prototype.GetStringEntry = r.prototype.GetStringEntry = function (a, b) {
      var c = this.ptr;
      n.prepare();
      a && "object" === typeof a && (a = a.ptr);
      b = b && "object" === typeof b ? b.ptr : V(b);
      return X($b(c, a, b));
    };
    r.prototype.NumEntries = r.prototype.NumEntries = function (a) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      return ac(c, a);
    };
    r.prototype.GetEntryName = r.prototype.GetEntryName = function (a, b) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      b && "object" === typeof b && (b = b.ptr);
      return X(bc(c, a, b));
    };
    r.prototype.__destroy__ = r.prototype.__destroy__ = function () {
      cc(this.ptr);
    };
    G.prototype = Object.create(p.prototype);
    G.prototype.constructor = G;
    G.prototype.__class__ = G;
    G.__cache__ = {};
    a.DracoInt16Array = G;
    G.prototype.GetValue = G.prototype.GetValue = function (a) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      return dc(c, a);
    };
    G.prototype.size = G.prototype.size = function () {
      return ec(this.ptr);
    };
    G.prototype.__destroy__ = G.prototype.__destroy__ = function () {
      fc(this.ptr);
    };
    H.prototype = Object.create(p.prototype);
    H.prototype.constructor = H;
    H.prototype.__class__ = H;
    H.__cache__ = {};
    a.DracoFloat32Array = H;
    H.prototype.GetValue = H.prototype.GetValue = function (a) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      return gc(c, a);
    };
    H.prototype.size = H.prototype.size = function () {
      return hc(this.ptr);
    };
    H.prototype.__destroy__ = H.prototype.__destroy__ = function () {
      ic(this.ptr);
    };
    O.prototype = Object.create(p.prototype);
    O.prototype.constructor = O;
    O.prototype.__class__ = O;
    O.__cache__ = {};
    a.GeometryAttribute = O;
    O.prototype.__destroy__ = O.prototype.__destroy__ = function () {
      jc(this.ptr);
    };
    K.prototype = Object.create(p.prototype);
    K.prototype.constructor = K;
    K.prototype.__class__ = K;
    K.__cache__ = {};
    a.DecoderBuffer = K;
    K.prototype.Init = K.prototype.Init = function (a, b) {
      var c = this.ptr;
      n.prepare();
      if ("object" == typeof a && "object" === typeof a) {
        var e = n.alloc(a, T);
        n.copy(a, T, e);
        a = e;
      }
      b && "object" === typeof b && (b = b.ptr);
      kc(c, a, b);
    };
    K.prototype.__destroy__ = K.prototype.__destroy__ = function () {
      lc(this.ptr);
    };
    g.prototype = Object.create(p.prototype);
    g.prototype.constructor = g;
    g.prototype.__class__ = g;
    g.__cache__ = {};
    a.Decoder = g;
    g.prototype.GetEncodedGeometryType = g.prototype.GetEncodedGeometryType = function (a) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      return mc(c, a);
    };
    g.prototype.DecodeBufferToPointCloud = g.prototype.DecodeBufferToPointCloud = function (a, b) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      b && "object" === typeof b && (b = b.ptr);
      return N(nc(c, a, b), x);
    };
    g.prototype.DecodeBufferToMesh = g.prototype.DecodeBufferToMesh = function (a, b) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      b && "object" === typeof b && (b = b.ptr);
      return N(oc(c, a, b), x);
    };
    g.prototype.GetAttributeId = g.prototype.GetAttributeId = function (a, b) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      b && "object" === typeof b && (b = b.ptr);
      return pc(c, a, b);
    };
    g.prototype.GetAttributeIdByName = g.prototype.GetAttributeIdByName = function (a, b) {
      var c = this.ptr;
      n.prepare();
      a && "object" === typeof a && (a = a.ptr);
      b = b && "object" === typeof b ? b.ptr : V(b);
      return qc(c, a, b);
    };
    g.prototype.GetAttributeIdByMetadataEntry = g.prototype.GetAttributeIdByMetadataEntry =
      function (a, b, d) {
        var c = this.ptr;
        n.prepare();
        a && "object" === typeof a && (a = a.ptr);
        b = b && "object" === typeof b ? b.ptr : V(b);
        d = d && "object" === typeof d ? d.ptr : V(d);
        return rc(c, a, b, d);
      };
    g.prototype.GetAttribute = g.prototype.GetAttribute = function (a, b) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      b && "object" === typeof b && (b = b.ptr);
      return N(sc(c, a, b), q);
    };
    g.prototype.GetAttributeByUniqueId = g.prototype.GetAttributeByUniqueId = function (a, b) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      b && "object" === typeof b && (b = b.ptr);
      return N(tc(c, a, b), q);
    };
    g.prototype.GetMetadata = g.prototype.GetMetadata = function (a) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      return N(uc(c, a), L);
    };
    g.prototype.GetAttributeMetadata = g.prototype.GetAttributeMetadata = function (a, b) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      b && "object" === typeof b && (b = b.ptr);
      return N(vc(c, a, b), L);
    };
    g.prototype.GetFaceFromMesh = g.prototype.GetFaceFromMesh = function (a, b, d) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      b && "object" === typeof b && (b = b.ptr);
      d && "object" === typeof d && (d = d.ptr);
      return !!wc(c, a, b, d);
    };
    g.prototype.GetTriangleStripsFromMesh = g.prototype.GetTriangleStripsFromMesh = function (
      a,
      b
    ) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      b && "object" === typeof b && (b = b.ptr);
      return xc(c, a, b);
    };
    g.prototype.GetTrianglesUInt16Array = g.prototype.GetTrianglesUInt16Array = function (a, b, d) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      b && "object" === typeof b && (b = b.ptr);
      d && "object" === typeof d && (d = d.ptr);
      return !!yc(c, a, b, d);
    };
    g.prototype.GetTrianglesUInt32Array = g.prototype.GetTrianglesUInt32Array = function (a, b, d) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      b && "object" === typeof b && (b = b.ptr);
      d && "object" === typeof d && (d = d.ptr);
      return !!zc(c, a, b, d);
    };
    g.prototype.GetAttributeFloat = g.prototype.GetAttributeFloat = function (a, b, d) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      b && "object" === typeof b && (b = b.ptr);
      d && "object" === typeof d && (d = d.ptr);
      return !!Ac(c, a, b, d);
    };
    g.prototype.GetAttributeFloatForAllPoints = g.prototype.GetAttributeFloatForAllPoints =
      function (a, b, d) {
        var c = this.ptr;
        a && "object" === typeof a && (a = a.ptr);
        b && "object" === typeof b && (b = b.ptr);
        d && "object" === typeof d && (d = d.ptr);
        return !!Bc(c, a, b, d);
      };
    g.prototype.GetAttributeIntForAllPoints = g.prototype.GetAttributeIntForAllPoints = function (
      a,
      b,
      d
    ) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      b && "object" === typeof b && (b = b.ptr);
      d && "object" === typeof d && (d = d.ptr);
      return !!Cc(c, a, b, d);
    };
    g.prototype.GetAttributeInt8ForAllPoints = g.prototype.GetAttributeInt8ForAllPoints = function (
      a,
      b,
      d
    ) {
      var c = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      b && "object" === typeof b && (b = b.ptr);
      d && "object" === typeof d && (d = d.ptr);
      return !!Dc(c, a, b, d);
    };
    g.prototype.GetAttributeUInt8ForAllPoints = g.prototype.GetAttributeUInt8ForAllPoints =
      function (a, b, d) {
        var c = this.ptr;
        a && "object" === typeof a && (a = a.ptr);
        b && "object" === typeof b && (b = b.ptr);
        d && "object" === typeof d && (d = d.ptr);
        return !!Ec(c, a, b, d);
      };
    g.prototype.GetAttributeInt16ForAllPoints = g.prototype.GetAttributeInt16ForAllPoints =
      function (a, b, d) {
        var c = this.ptr;
        a && "object" === typeof a && (a = a.ptr);
        b && "object" === typeof b && (b = b.ptr);
        d && "object" === typeof d && (d = d.ptr);
        return !!Fc(c, a, b, d);
      };
    g.prototype.GetAttributeUInt16ForAllPoints = g.prototype.GetAttributeUInt16ForAllPoints =
      function (a, b, d) {
        var c = this.ptr;
        a && "object" === typeof a && (a = a.ptr);
        b && "object" === typeof b && (b = b.ptr);
        d && "object" === typeof d && (d = d.ptr);
        return !!Gc(c, a, b, d);
      };
    g.prototype.GetAttributeInt32ForAllPoints = g.prototype.GetAttributeInt32ForAllPoints =
      function (a, b, d) {
        var c = this.ptr;
        a && "object" === typeof a && (a = a.ptr);
        b && "object" === typeof b && (b = b.ptr);
        d && "object" === typeof d && (d = d.ptr);
        return !!Hc(c, a, b, d);
      };
    g.prototype.GetAttributeUInt32ForAllPoints = g.prototype.GetAttributeUInt32ForAllPoints =
      function (a, b, d) {
        var c = this.ptr;
        a && "object" === typeof a && (a = a.ptr);
        b && "object" === typeof b && (b = b.ptr);
        d && "object" === typeof d && (d = d.ptr);
        return !!Ic(c, a, b, d);
      };
    g.prototype.GetAttributeDataArrayForAllPoints = g.prototype.GetAttributeDataArrayForAllPoints =
      function (a, b, d, e, f) {
        var c = this.ptr;
        a && "object" === typeof a && (a = a.ptr);
        b && "object" === typeof b && (b = b.ptr);
        d && "object" === typeof d && (d = d.ptr);
        e && "object" === typeof e && (e = e.ptr);
        f && "object" === typeof f && (f = f.ptr);
        return !!Jc(c, a, b, d, e, f);
      };
    g.prototype.SkipAttributeTransform = g.prototype.SkipAttributeTransform = function (a) {
      var b = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      Kc(b, a);
    };
    g.prototype.__destroy__ = g.prototype.__destroy__ = function () {
      Lc(this.ptr);
    };
    y.prototype = Object.create(p.prototype);
    y.prototype.constructor = y;
    y.prototype.__class__ = y;
    y.__cache__ = {};
    a.Mesh = y;
    y.prototype.num_faces = y.prototype.num_faces = function () {
      return Mc(this.ptr);
    };
    y.prototype.num_attributes = y.prototype.num_attributes = function () {
      return Nc(this.ptr);
    };
    y.prototype.num_points = y.prototype.num_points = function () {
      return Oc(this.ptr);
    };
    y.prototype.__destroy__ = y.prototype.__destroy__ = function () {
      Pc(this.ptr);
    };
    Q.prototype = Object.create(p.prototype);
    Q.prototype.constructor = Q;
    Q.prototype.__class__ = Q;
    Q.__cache__ = {};
    a.VoidPtr = Q;
    Q.prototype.__destroy__ = Q.prototype.__destroy__ = function () {
      Qc(this.ptr);
    };
    I.prototype = Object.create(p.prototype);
    I.prototype.constructor = I;
    I.prototype.__class__ = I;
    I.__cache__ = {};
    a.DracoInt32Array = I;
    I.prototype.GetValue = I.prototype.GetValue = function (a) {
      var b = this.ptr;
      a && "object" === typeof a && (a = a.ptr);
      return Rc(b, a);
    };
    I.prototype.size = I.prototype.size = function () {
      return Sc(this.ptr);
    };
    I.prototype.__destroy__ = I.prototype.__destroy__ = function () {
      Tc(this.ptr);
    };
    L.prototype = Object.create(p.prototype);
    L.prototype.constructor = L;
    L.prototype.__class__ = L;
    L.__cache__ = {};
    a.Metadata = L;
    L.prototype.__destroy__ = L.prototype.__destroy__ = function () {
      Uc(this.ptr);
    };
    (function () {
      function c() {
        a.OK = Vc();
        a.DRACO_ERROR = Wc();
        a.IO_ERROR = Xc();
        a.INVALID_PARAMETER = Yc();
        a.UNSUPPORTED_VERSION = Zc();
        a.UNKNOWN_VERSION = $c();
        a.DT_INVALID = ad();
        a.DT_INT8 = bd();
        a.DT_UINT8 = cd();
        a.DT_INT16 = dd();
        a.DT_UINT16 = ed();
        a.DT_INT32 = fd();
        a.DT_UINT32 = gd();
        a.DT_INT64 = hd();
        a.DT_UINT64 = id();
        a.DT_FLOAT32 = jd();
        a.DT_FLOAT64 = kd();
        a.DT_BOOL = ld();
        a.DT_TYPES_COUNT = md();
        a.INVALID_GEOMETRY_TYPE = nd();
        a.POINT_CLOUD = od();
        a.TRIANGULAR_MESH = pd();
        a.ATTRIBUTE_INVALID_TRANSFORM = qd();
        a.ATTRIBUTE_NO_TRANSFORM = rd();
        a.ATTRIBUTE_QUANTIZATION_TRANSFORM = sd();
        a.ATTRIBUTE_OCTAHEDRON_TRANSFORM = td();
        a.INVALID = ud();
        a.POSITION = vd();
        a.NORMAL = wd();
        a.COLOR = xd();
        a.TEX_COORD = yd();
        a.GENERIC = zd();
      }
      Ba ? c() : Da.unshift(c);
    })();
    if ("function" === typeof a.onModuleParsed) a.onModuleParsed();
    return m;
  };
})();
"object" === typeof exports && "object" === typeof module
  ? (module.exports = DracoDecoderModule)
  : "function" === typeof define && define.amd
  ? define([], function () {
      return DracoDecoderModule;
    })
  : "object" === typeof exports && (exports.DracoDecoderModule = DracoDecoderModule);
