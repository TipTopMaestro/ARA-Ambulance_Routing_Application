import fs from 'fs';

const data = JSON.parse(fs.readFileSync('src/main/resources/osm_data.json', 'utf8'));

const nodes = new Map();
const nodeUsage = new Map(); // to find intersections

data.elements.forEach(el => {
    if (el.type === 'node') {
        nodes.set(el.id, { lat: el.lat, lon: el.lon });
    }
});

data.elements.forEach(el => {
    if (el.type === 'way') {
        el.nodes.forEach(nodeId => {
            nodeUsage.set(nodeId, (nodeUsage.get(nodeId) || 0) + 1);
        });
    }
});

const intersections = Array.from(nodeUsage.entries())
    .filter(([id, count]) => count > 1)
    .map(([id]) => id);

function findClosest(lat, lon) {
    let minD = Infinity;
    let closest = null;
    intersections.forEach(id => {
        const node = nodes.get(id);
        if (!node) return;
        const d = Math.pow(node.lat - lat, 2) + Math.pow(node.lon - lon, 2);
        if (d < minD) {
            minD = d;
            closest = id;
        }
    });
    return closest;
}

console.log('Polymedic closest intersection:', findClosest(7.3031, 125.6786));
console.log('Good Shepherd closest intersection:', findClosest(7.2940, 125.6782));
console.log('Rivera Medical closest intersection:', findClosest(7.3128, 125.6855));
console.log('City Hall closest intersection:', findClosest(7.3081, 125.6841));

const someIntersections = intersections.slice(0, 4);
console.log('Some general intersections for ambulances:', someIntersections);
