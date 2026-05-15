<template>
  <div class="map-root" ref="mapContainer" style="height: 100%; width: 100%" />
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

const props = defineProps({
  pathCoordinates: { type: Array, default: () => [] },
  allNodes: { type: Array, default: () => [] },
  onNodeClick: { type: Function, default: () => {} },
  selectedSource: { type: String, default: null },
  selectedDestination: { type: String, default: null },
  allEdges: { type: Array, default: () => [] },
  currentTraversedNode: { type: String, default: null },
  currentRelaxedEdge: { type: Object, default: null },
  visitedNodes: { type: Array, default: () => [] }
})

const mapContainer = ref(null)
let mapInstance = null
let polylineLayer = null
let markersLayer = L.layerGroup()
let backgroundMarkersLayer = L.layerGroup()
let edgeLinesLayer = L.layerGroup()
let relaxedEdgeLine = null

onMounted(() => {
  if (!mapContainer.value) return

  mapInstance = L.map(mapContainer.value).setView([7.3081, 125.6841], 14)
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
  }).addTo(mapInstance)

  markersLayer.addTo(mapInstance)
  backgroundMarkersLayer.addTo(mapInstance)
  edgeLinesLayer.addTo(mapInstance)

  updateMap()
})

const updateMap = () => {
  if (!mapInstance) return

  const nodeLookup = {}
  props.allNodes.forEach(n => { nodeLookup[n.id] = n })

  // Current relaxed edge
  if (relaxedEdgeLine) mapInstance.removeLayer(relaxedEdgeLine)
  if (props.currentRelaxedEdge) {
    const source = nodeLookup[props.currentRelaxedEdge.sourceId]
    const target = nodeLookup[props.currentRelaxedEdge.targetId]
    if (source && target) {
      relaxedEdgeLine = L.polyline([[source.latitude, source.longitude], [target.latitude, target.longitude]], {
        color: '#fbbf24',
        weight: 8,
        opacity: 1,
        zIndex: 1000
      }).addTo(mapInstance)
    }
  }

  // All edges (negative highlight)
  edgeLinesLayer.clearLayers()
  if (props.allEdges && props.allEdges.length > 0) {
    props.allEdges.filter(e => e.weight < 0).forEach(edge => {
      const source = nodeLookup[edge.source]
      const target = nodeLookup[edge.target]
      if (source && target) {
        const line = L.polyline([[source.latitude, source.longitude], [target.latitude, target.longitude]], {
          color: '#7c3aed',
          weight: 4,
          dashArray: '5, 10',
          opacity: 0.8
        })
        line.bindTooltip(`Negative Edge: ${edge.weight.toFixed(1)}`, { sticky: true })
        edgeLinesLayer.addLayer(line)
      }
    })
  }

  // Background markers
  backgroundMarkersLayer.clearLayers()
  const visitedSet = new Set(props.visitedNodes)

  props.allNodes.forEach(node => {
    const isHospital = node.id.startsWith('H')
    const isAmbulance = node.id.startsWith('AMB')
    const isSelected = node.id === props.selectedSource || node.id === props.selectedDestination
    const isTraversed = node.id === props.currentTraversedNode
    const isVisited = visitedSet.has(node.id)
    
    let fillColor = '#3b82f6' // Default blue
    if (isTraversed) fillColor = '#f43f5e'
    else if (isSelected) fillColor = '#10b981'
    else if (isHospital) fillColor = '#ef4444'
    else if (isAmbulance) fillColor = '#f59e0b'
    else if (isVisited) fillColor = '#fbbf24' // Amber for visited

    const marker = L.circleMarker([node.latitude, node.longitude], {
      radius: isTraversed ? 12 : (isSelected ? 10 : (isHospital ? 8 : (isAmbulance ? 7 : 5))),
      fillColor: fillColor,
      color: isTraversed ? '#be123c' : (isSelected ? '#064e3b' : (isVisited ? '#d97706' : '#ffffff')),
      weight: (isSelected || isTraversed || isVisited) ? 4 : 2,
      opacity: 1,
      fillOpacity: 0.9
    })

    marker.on('click', () => {
      props.onNodeClick(node.id, node.latitude, node.longitude, node.name)
    })

    marker.bindTooltip(node.name || node.id, { permanent: false, direction: 'top' })
    backgroundMarkersLayer.addLayer(marker)
  })

  // Path polyline
  if (polylineLayer) mapInstance.removeLayer(polylineLayer)
  markersLayer.clearLayers()

  if (props.pathCoordinates && props.pathCoordinates.length >= 2) {
    const pathCoordsList = props.pathCoordinates.map((node) => [node.lat, node.lng])
    
    polylineLayer = L.polyline(pathCoordsList, {
      color: '#22c55e',
      opacity: 0.9,
      weight: 8,
    }).addTo(mapInstance)

    props.pathCoordinates.forEach((node, index) => {
      const isStart = index === 0
      const isEnd = index === props.pathCoordinates.length - 1
      if (!isStart && !isEnd) return 

      let fillColor = isStart ? '#22c55e' : '#15803d'
      let radius = 10

      const marker = L.circleMarker([node.lat, node.lng], {
        radius: radius,
        fillColor: fillColor,
        color: '#ffffff',
        weight: 3,
        opacity: 1,
        fillOpacity: 1
      })
      markersLayer.addLayer(marker)
    })
    
    mapInstance.fitBounds(polylineLayer.getBounds(), { padding: [50, 50] })
  }
}

watch(() => props, () => {
  updateMap()
}, { deep: true })

onUnmounted(() => {
  if (mapInstance) {
    mapInstance.remove()
  }
})
</script>
