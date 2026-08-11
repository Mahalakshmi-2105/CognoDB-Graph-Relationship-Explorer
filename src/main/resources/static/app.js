const statusEl = document.getElementById('status');
const profile = document.getElementById('profile');
const results = document.getElementById('results');

const esc = (s) =>
    String(s ?? '').replace(/[&<>"']/g, c => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    }[c]));

async function get(url) {
    const response = await fetch(url);

    if (!response.ok) {
        if (response.status === 404) {
            throw new Error('Person not found.');
        }

        throw new Error('Unable to reach CognoDB. Please try again.');
    }

    return response.json();
}

function list(title, items) {
    return `
        <div class="panel">
            <div class="panel-header">
                <div>
                    <h2>${title}</h2>
                    <p class="muted">${items.length} result(s)</p>
                </div>
            </div>

            <div class="people">
                ${
                    items.length
                        ? items.map(p => `
                            <div class="person">
                                <strong>${esc(p.name)}</strong>
                                <span class="muted">
                                    ID ${esc(p.id)} • Age ${esc(p.age)}
                                </span>
                            </div>
                        `).join('')
                        : `
                            <div class="empty">
                                No connections found.
                            </div>
                        `
                }
            </div>
        </div>
    `;
}

async function searchPerson() {

    const input = document.getElementById('personId');
    const id = input.value.trim();

    if (!id) {
        statusEl.textContent = 'Please enter a Person ID.';
        profile.classList.add('hidden');
        results.classList.add('hidden');
        input.focus();
        return;
    }

    if (!/^\d+$/.test(id)) {
        statusEl.textContent = 'Person ID must contain numbers only.';
        profile.classList.add('hidden');
        results.classList.add('hidden');
        input.focus();
        return;
    }

    statusEl.textContent = 'Loading graph data...';
    profile.classList.add('hidden');
    results.classList.add('hidden');

    try {

        const [person, friends, fof, network] = await Promise.all([
            get(`/api/person/${id}`),
            get(`/api/person/${id}/friends`),
            get(`/api/person/${id}/friends-of-friends`),
            get(`/api/person/${id}/network`)
        ]);

        profile.innerHTML = `
            <div class="profile-content">
                <div>
                    <p class="eyebrow">PERSON</p>
                    <h2>${esc(person.name)}</h2>
                    <p class="muted">
                        Person ID: ${esc(person.id)}
                    </p>
                </div>

                <div class="age">
                    <span>AGE</span>
                    <strong>${esc(person.age)}</strong>
                </div>
            </div>
        `;

        profile.classList.remove('hidden');

        results.innerHTML =
            list('Direct Friends', friends) +
            list('Friends of Friends (2 hops)', fof) +
            list('Network (up to 3 hops)', network);

        results.classList.remove('hidden');

        statusEl.textContent =
            `Graph loaded successfully for Person ${esc(person.id)}.`;

    } catch (error) {

        profile.classList.add('hidden');
        results.classList.add('hidden');

        statusEl.textContent = error.message;

    }
}

document
    .getElementById('personId')
    .addEventListener('keydown', event => {
        if (event.key === 'Enter') {
            searchPerson();
        }
    });

document
    .getElementById('searchButton')
    ?.addEventListener('click', searchPerson);