# Security Policy

CleanCut is committed to ensuring the security and privacy of our users. Because CleanCut operates 100% on-device with zero network transmissions of personal photos, user privacy and data security are foundational principles of the project.

---

## Supported Versions

We provide security updates and patches for the following versions of CleanCut:

| Version | Supported          |
| ------- | ------------------ |
| 0.1.x   | :white_check_mark: |
| < 0.1.0 | :x:                |

---

## Reporting a Vulnerability

If you discover a potential security vulnerability or privacy flaw within CleanCut, please report it responsibly:

1. **Do NOT disclose publicly**: Please refrain from opening public GitHub issues or discussions for undisclosed security vulnerabilities.
2. **Contact via Email**: Send an email to **[security@cleancut.dev](mailto:security@cleancut.dev)** with the subject line `[SECURITY VULNERABILITY] <Brief Description>`.
3. **Information to Include**:
   - Detailed description of the vulnerability and its potential impact.
   - Step-by-step reproduction instructions or Proof of Concept (PoC).
   - Affected CleanCut version(s) and Android OS versions / device models.
   - Any suggested mitigations or patches if available.

---

## Response Timeline

- **Initial Acknowledgment**: Within 48 hours of report receipt.
- **Assessment & Triage**: Within 5 business days, confirming vulnerability validity and severity.
- **Fix & Disclosure**: We will collaborate with you on a patch timeline and credit your contribution in release notes upon public disclosure (unless you prefer anonymity).

---

## Core Privacy & Security Principles

1. **Zero External Data Exfiltration**: CleanCut does not transmit images, bitmaps, or segmented masks to any remote server or cloud API. All processing occurs strictly on the local hardware.
2. **Scoped Storage Compliance**: Exports utilize modern Android MediaStore Scoped Storage APIs without requesting legacy broad storage permissions (`READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE`).
3. **App Cache Safety**: Intermediate cached bitmaps are stored strictly in private application cache directories and cleaned up properly.
