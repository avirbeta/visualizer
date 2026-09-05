#!/usr/bin/env python3
"""Generates Visualizer's own UI sound resources (WAV) and a simple icon (PNG).

All assets are generated procedurally, so they are our own and freely usable.
Run from the project root:  python3 tools/gen_assets.py
"""
import math
import os
import struct
import wave
import zlib

RES = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "visualizer")
SOUNDS = os.path.join(RES, "sounds")
os.makedirs(SOUNDS, exist_ok=True)

RATE = 44100


def write_wav(name, samples):
    path = os.path.join(SOUNDS, name + ".wav")
    with wave.open(path, "w") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        w.writeframes(b"".join(struct.pack("<h", int(max(-1.0, min(1.0, s)) * 32767)) for s in samples))
    print("wrote", path, len(samples), "samples")


def tone(duration, freq0, freq1, volume=0.7, extra=(0.0,)):
    """Sine with linear frequency sweep and exponential decay envelope."""
    n = int(RATE * duration)
    out = []
    for i in range(n):
        t = i / float(RATE)
        p = t / float(duration)
        freq = freq0 + (freq1 - freq0) * p
        phase = 2.0 * math.pi * freq * t
        env = math.exp(-3.5 * p)
        s = volume * env * math.sin(phase)
        for e in extra:
            s += volume * 0.5 * env * math.sin(2.0 * math.pi * e * t)
        out.append(s)
    return out


# hover: short, soft tick
write_wav("hover", tone(0.045, 1050.0, 900.0, volume=0.35))
# click: crisp tick
write_wav("click", tone(0.06, 1450.0, 1100.0, volume=0.55))
# open: rising chirp with a fifth
write_wav("open", tone(0.13, 480.0, 860.0, volume=0.5, extra=(720.0,)))
# close: falling chirp
write_wav("close", tone(0.11, 760.0, 420.0, volume=0.5, extra=(570.0,)))
# notification: pleasant two-tone ding
write_wav("notification", tone(0.24, 880.0, 880.0, volume=0.5, extra=(1174.66,)))

# ---------------- icon.png ----------------
SIZE = 128
BG = (11, 14, 20)
ACCENT = (46, 204, 113)
LIGHT = (242, 244, 248)
R = 26.0  # outer rounded radius


def rounded_corner(px, py, cx, cy, r):
    if not (cx - r < px < cx + r and cy - r < py < cy + r):
        return True
    dx = max(abs(px - cx) - (r - r), 0)
    dy = max(abs(py - cy), 0)
    return (px - cx) ** 2 + (py - cy) ** 2 >= r * r if (px - cx) ** 2 + (py - cy) ** 2 > (r - 1) ** 2 else False


def inside_rounded(x, y):
    # points outside the four corner circles are clipped
    if x < R and y < R:
        return (x - R) ** 2 + (y - R) ** 2 <= R * R
    if x >= SIZE - R and y < R:
        return (x - (SIZE - R)) ** 2 + (y - R) ** 2 <= R * R
    if x < R and y >= SIZE - R:
        return (x - R) ** 2 + (y - (SIZE - R)) ** 2 <= R * R
    if x >= SIZE - R and y >= SIZE - R:
        return (x - (SIZE - R)) ** 2 + (y - (SIZE - R)) ** 2 <= R * R
    return True


def in_v(x, y):
    # A centered "V" composed of two thick strokes with rounded feel.
    cx = SIZE / 2.0
    y0 = 30.0
    y1 = 98.0
    x0 = 34.0
    x1 = cx
    x2 = SIZE - 34.0
    # left stroke line from (x0,y0) to (x1,y1)
    def dist_seg(px, py, ax, ay, bx, by):
        dx = bx - ax
        dy = by - ay
        t = ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy)
        t = max(0.0, min(1.0, t))
        qx = ax + dx * t
        qy = ay + dy * t
        return math.hypot(px - qx, py - qy)

    w = 13.0
    return (dist_seg(x, y, x0, y0, x1, y1) < w) or (dist_seg(x, y, x2, y0, x1, y1) < w)


rows = []
for y in range(SIZE):
    row = bytearray()
    row.append(0)  # filter: none
    for x in range(SIZE):
        if not inside_rounded(x, y):
            row.extend((0, 0, 0, 0))
            continue
        # subtle vertical gradient over the dark card
        t = y / float(SIZE)
        r = int(BG[0] + 6 * t)
        g = int(BG[1] + 8 * t)
        b = int(BG[2] + 10 * t)
        if in_v(x, y):
            glow = max(0.0, 1.0 - math.hypot(x - SIZE / 2.0, y - 64.0) / 90.0) * 40.0
            r = int(min(255, ACCENT[0] + glow))
            g = int(min(255, ACCENT[1] + glow))
            b = int(min(255, ACCENT[2] + glow))
        # top accent bar
        if 10 <= y <= 14 and inside_rounded(x, y):
            r, g, b = ACCENT
        row.extend((r, g, b, 255))
    rows.append(bytes(row))

raw = b"".join(rows)


def chunk(tag, data):
    c = struct.pack(">I", len(data))
    c += tag + data
    c += struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)
    return c


ihdr = struct.pack(">IIBBBBB", SIZE, SIZE, 8, 6, 0, 0, 0)
png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr) + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b"")
icon_path = os.path.join(RES, "icon.png")
with open(icon_path, "wb") as f:
    f.write(png)
print("wrote", icon_path)

# also copy icon to root assets/visualizer for fabric.mod reference
print("done")