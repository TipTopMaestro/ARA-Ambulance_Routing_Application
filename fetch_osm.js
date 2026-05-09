import fs from 'fs';
import https from 'https';

const query = `[out:json];way["highway"](7.29,125.67,7.32,125.69);(._;>;);out;`;
const options = {
    hostname: 'overpass-api.de',
    port: 443,
    path: `/api/interpreter?data=${encodeURIComponent(query)}`,
    method: 'GET',
    headers: {
        'User-Agent': 'BellmanFordAmbulanceRouter/1.0'
    }
};

https.get(options, (res) => {
    let data = '';
    res.on('data', chunk => data += chunk);
    res.on('end', () => {
        fs.writeFileSync('src/main/resources/osm_data.json', data);
        console.log('Saved to src/main/resources/osm_data.json, size:', data.length);
    });
}).on('error', (err) => {
    console.error(err);
});