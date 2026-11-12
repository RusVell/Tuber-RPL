(() => {
    const semester = document.querySelector('#ujian-semester');
    const kelas = document.querySelector('#ujian-kelas');
    const options = [...kelas.querySelectorAll('option[data-semester]')];
    const help = document.querySelector('#exam-class-help');

    function filterKelas() {
        const available = options.filter(option => option.dataset.semester === semester.value);
        kelas.replaceChildren(new Option(available.length ? 'Pilih Mata Kuliah / Kelas' : 'Belum ada kelas di semester ini', ''));
        kelas.options[0].disabled = true;
        available.forEach(option => kelas.append(option));
        kelas.disabled = available.length === 0;
        kelas.form.querySelector('[type="submit"]').disabled = available.length === 0;
        help.textContent = available.length ? 'Kelas ditampilkan sesuai semester yang dipilih.'
            : 'Pilih semester yang memiliki kelas untuk menambahkan jadwal ujian.';
    }
    semester.addEventListener('change', filterKelas);
    filterKelas();
})();
