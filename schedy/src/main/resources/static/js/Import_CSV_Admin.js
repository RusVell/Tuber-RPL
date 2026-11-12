(() => {
    const types = {
        mahasiswa: { label: 'Akun Mahasiswa dan Dosen', header: 'nama,NPM/NIDN,role' },
        'mata-kuliah': { label: 'Mata Kuliah', header: 'kode_matkul,nama_matkul,sks,nama_kelas,nip_dosen' },
        'jadwal-kuliah': { label: 'Jadwal Kuliah', header: 'kode_matkul,nama_kelas,hari,jam_mulai,jam_selesai,ruangan' },
        'jadwal-ujian': { label: 'Jadwal Ujian', header: 'kode_matkul,nama_kelas,jenis_ujian,tanggal,jam_mulai,jam_selesai,ruangan' }
    };
    const tabs = [...document.querySelectorAll('.tab')];
    const input = document.querySelector('#csv-file');
    const zone = document.querySelector('#drop-zone');
    const status = document.querySelector('#upload-status');
    const submit = document.querySelector('#submit-upload');
    const form = document.querySelector('#csv-upload-form');
    let type;
    let file;
    let uploading = false;

    function updateSubmit() {
        submit.disabled = uploading || !file || (type !== 'mahasiswa' && !form.elements.idSemester.value);
    }

    function message(text, error = false) {
        status.textContent = text;
        status.classList.toggle('error', error);
    }

    function selectFile(next) {
        if (uploading) return;
        file = null;
        input.value = '';
        message('');
        if (next && (!/\.csv$/i.test(next.name) || next.size === 0 || next.size > 10 * 1024 * 1024)) {
            message('Pilih file CSV berisi data dengan ukuran maksimum 10 MB.', true);
        } else {
            file = next;
        }
        zone.classList.toggle('has-file', Boolean(file));
        document.querySelector('.upload-empty').hidden = Boolean(file);
        document.querySelector('.upload-selected').hidden = !file;
        document.querySelector('#file-name').textContent = file ? file.name : '';
        document.querySelector('#file-size').textContent = file ? `${(file.size / 1024).toFixed(1)} KB` : '';
        updateSubmit();
    }

    function activate(tab) {
        if (uploading) return;
        type = tab.dataset.type;
        form.elements.type.value = type;
        const returnTo = document.querySelector('input[name="returnTo"]');
        if (returnTo) {
            const target = new URL(returnTo.value, window.location.href);
            target.searchParams.set('type', type);
            returnTo.value = target.pathname + target.search;
        }
        tabs.forEach(item => {
            const active = item === tab;
            item.classList.toggle('active', active);
            item.setAttribute('aria-selected', String(active));
            item.tabIndex = active ? 0 : -1;
        });
        selectFile(null);
        document.querySelector('#upload-title').textContent = `Unggah CSV ${types[type].label}`;
        document.querySelector('#upload-help').textContent = `Kolom ${types[type].label}: ${types[type].header.replaceAll(',', ', ')}.`;
    }

    tabs.forEach((tab, index) => {
        tab.addEventListener('click', () => activate(tab));
        tab.addEventListener('keydown', event => {
            if (!['ArrowLeft', 'ArrowRight', 'Home', 'End'].includes(event.key)) return;
            event.preventDefault();
            const next = event.key === 'Home' ? 0 : event.key === 'End' ? tabs.length - 1
                : (index + (event.key === 'ArrowRight' ? 1 : -1) + tabs.length) % tabs.length;
            activate(tabs[next]);
            tabs[next].focus();
        });
    });
    zone.addEventListener('click', () => { if (!uploading) input.click(); });
    zone.addEventListener('keydown', event => {
        if (!uploading && event.target === zone && ['Enter', ' '].includes(event.key)) {
            event.preventDefault();
            input.click();
        }
    });
    input.addEventListener('change', () => selectFile(input.files[0]));
    document.querySelector('#remove-file').addEventListener('click', event => {
        event.stopPropagation();
        selectFile(null);
    });
    zone.addEventListener('dragover', event => {
        event.preventDefault();
        zone.classList.add('dragging');
    });
    zone.addEventListener('dragleave', () => zone.classList.remove('dragging'));
    zone.addEventListener('drop', event => {
        event.preventDefault();
        zone.classList.remove('dragging');
        if (uploading) return;
        if (event.dataTransfer.files.length !== 1) {
            selectFile(null);
            message('Pilih satu file CSV untuk setiap impor.', true);
            return;
        }
        selectFile(event.dataTransfer.files[0]);
    });
    document.querySelector('#download-template').addEventListener('click', () => {
        const url = URL.createObjectURL(new Blob([types[type].header + '\r\n'], { type: 'text/csv;charset=utf-8' }));
        const link = document.createElement('a');
        link.href = url;
        link.download = `template-${type}.csv`;
        link.click();
        setTimeout(() => URL.revokeObjectURL(url), 1000);
    });
    form.addEventListener('submit', async event => {
        event.preventDefault();
        if (uploading || !file) return;
        if (type !== 'mahasiswa' && !form.elements.idSemester.value) {
            message('Pilih semester terlebih dahulu.', true);
            return;
        }
        const data = new FormData(form);
        // File disimpan terpisah agar unggahan melalui drag-and-drop ikut terkirim.
        data.set('file', file);
        uploading = true;
        updateSubmit();
        tabs.forEach(tab => { tab.disabled = true; });
        document.querySelector('#semester-aktif').disabled = true;
        document.querySelector('#remove-file').disabled = true;
        form.setAttribute('aria-busy', 'true');
        message('Sedang mengimpor CSV...');
        try {
            const response = await fetch(form.action, {
                method: 'POST',
                body: data,
                headers: { Accept: 'application/json' }
            });
            const result = await response.json().catch(() => null);
            if (!response.ok || !result?.success) {
                message(result?.message || (response.status === 413
                    ? 'Ukuran file CSV maksimum 10 MB.'
                    : 'Impor CSV gagal. Periksa file dan coba kembali.'), true);
                return;
            }
            uploading = false;
            selectFile(null);
            message(result.message);
        } catch (error) {
            message('Hasil impor belum dapat dipastikan karena koneksi terputus. Periksa Data Akademik sebelum mengulang impor.', true);
        } finally {
            uploading = false;
            tabs.forEach(tab => { tab.disabled = false; });
            document.querySelector('#semester-aktif').disabled = !form.elements.idSemester.value;
            document.querySelector('#remove-file').disabled = false;
            form.removeAttribute('aria-busy');
            updateSubmit();
        }
    });
    const requested = new URLSearchParams(window.location.search).get('type');
    activate(tabs.find(tab => tab.dataset.type === requested) || tabs[0]);
})();
