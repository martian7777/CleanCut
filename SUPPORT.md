# Support & Community

Welcome to the CleanCut community! We are here to help you get the most out of CleanCut.

---

## 💬 Getting Help

### 1. Documentation
Before asking a question, please check the project documentation:
- [Main README](README.md)
- [Architecture Guide](docs/architecture.md)
- [ML Pipeline Guide](docs/ml-pipeline.md)
- [Setup & Build Guide](docs/setup.md)
- [Release Guide](docs/release.md)

### 2. GitHub Discussions & Issues
- **Bug Reports**: If you found a bug or unexpected behavior, open a [Bug Report](https://github.com/martian7777/CleanCut/issues/new?template=bug_report.md).
- **Feature Requests**: Have an idea for CleanCut? Submit a [Feature Request](https://github.com/martian7777/CleanCut/issues/new?template=feature_request.md).
- **Questions & Discussions**: Use GitHub Discussions for general questions, integration advice, and community ideas.

---

## ❓ Frequently Asked Questions (FAQ)

### Q: Does CleanCut require internet access to remove backgrounds?
**A:** No. CleanCut executes ML inference 100% locally on your Android device using Google ML Kit. No photos or data are transmitted over the network.

### Q: Why does the first segmentation take slightly longer?
**A:** On the initial run, Google Play Services may download or initialize the on-device ML model. Subsequent runs execute in milliseconds.

### Q: What image formats are supported?
**A:** CleanCut supports standard photo formats including JPEG, PNG, WEBP, and HEIC. Exports are generated as high-fidelity transparent PNGs.

### Q: How do I report a security vulnerability?
**A:** Please refer to our [Security Policy](SECURITY.md) and email [security@cleancut.dev](mailto:security@cleancut.dev).
