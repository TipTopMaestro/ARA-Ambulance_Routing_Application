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
  staticEdges: { type: Array, default: () => [] },
  showGraphEdges: { type: Boolean, default: true },
  showNodeIds: { type: Boolean, default: false },
  showMapNodes: { type: Boolean, default: true },
  currentTraversedNode: { type: String, default: null },
  currentRelaxedEdge: { type: Object, default: null },
  visitedNodes: { type: Array, default: () => [] },
  viewCenter: { type: Array, default: () => [7.3081, 125.6841] },
  viewZoom: { type: Number, default: 14 }
})

const emit = defineEmits(['view-changed'])

const mapContainer = ref(null)
let mapInstance = null
let polylineLayer = null
let markersLayer = L.layerGroup()
let backgroundMarkersLayer = L.layerGroup()
let staticEdgeLinesLayer = L.layerGroup()
let algoEdgeLinesLayer = L.layerGroup()
let negativeCycleEdgesLayer = L.layerGroup()
let relaxedEdgeLine = null
let resizeObserver = null

// Optimized marker storage
const markerMap = {}

onMounted(() => {
  if (!mapContainer.value) return

  mapInstance = L.map(mapContainer.value).setView(props.viewCenter, props.viewZoom)
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
  }).addTo(mapInstance)

  mapInstance.on('moveend', () => {
    emit('view-changed', {
      center: [mapInstance.getCenter().lat, mapInstance.getCenter().lng],
      zoom: mapInstance.getZoom()
    })
  })

  mapInstance.on('zoomend', () => {
    emit('view-changed', {
      center: [mapInstance.getCenter().lat, mapInstance.getCenter().lng],
      zoom: mapInstance.getZoom()
    })
  })

  staticEdgeLinesLayer.addTo(mapInstance)
  algoEdgeLinesLayer.addTo(mapInstance)
  backgroundMarkersLayer.addTo(mapInstance)
  markersLayer.addTo(mapInstance)

  resizeObserver = new ResizeObserver(() => {
    if (mapInstance) {
      mapInstance.invalidateSize()
    }
  })
  resizeObserver.observe(mapContainer.value)

  syncMarkers()
  updateStaticLayers()
  updateAlgoLayers()
  updatePathLayer()
})

const syncMarkers = () => {
  if (!mapInstance) return
  
  const nodeIds = new Set(props.allNodes.map(n => n.id))
  
  // Cleanup old markers
  Object.keys(markerMap).forEach(id => {
    if (!nodeIds.has(id)) {
      backgroundMarkersLayer.removeLayer(markerMap[id])
      delete markerMap[id]
    }
  })

  // Add new markers if visibility is enabled
  if (props.showMapNodes) {
    props.allNodes.forEach(node => {
      if (!markerMap[node.id]) {
        const marker = L.circleMarker([node.latitude, node.longitude], {
          pane: 'markerPane'
        })
        marker.on('click', () => {
          props.onNodeClick(node.id, node.latitude, node.longitude, node.name)
        })
        markerMap[node.id] = marker
        backgroundMarkersLayer.addLayer(marker)
      }
    })
    if (!mapInstance.hasLayer(backgroundMarkersLayer)) {
      backgroundMarkersLayer.addTo(mapInstance)
    }
  } else {
    // Hide layer if disabled
    if (mapInstance.hasLayer(backgroundMarkersLayer)) {
      mapInstance.removeLayer(backgroundMarkersLayer)
    }
  }
}

const updateNodeStyles = () => {
  const visitedSet = new Set(props.visitedNodes)
  
  props.allNodes.forEach(node => {
    const marker = markerMap[node.id]
    if (!marker) return

    const isHospital = node.id.startsWith('H')
    const isAmbulance = node.id.startsWith('AMB')
    const isSelected = node.id === props.selectedSource || node.id === props.selectedDestination
    const isTraversed = node.id === props.currentTraversedNode
    const isVisited = visitedSet.has(node.id)
    
    let fillColor = '#3b82f6'
    if (isTraversed) fillColor = '#f43f5e'
    else if (isSelected) fillColor = '#10b981'
    else if (isHospital) fillColor = '#ef4444'
    else if (isAmbulance) fillColor = '#f59e0b'

    marker.setStyle({
      radius: isTraversed ? 12 : (isSelected ? 10 : (isHospital ? 8 : (isAmbulance ? 7 : 5))),
      fillColor: fillColor,
      color: isTraversed ? '#be123c' : (isSelected ? '#064e3b' : '#ffffff'),
      weight: (isSelected || isTraversed) ? 4 : 2,
      opacity: 1,
      fillOpacity: 0.9
    })

    // Update tooltips reactively
    const currentTooltip = marker.getTooltip()
    const isCurrentlyPermanent = currentTooltip?.options?.permanent || false
    const desiredText = props.showNodeIds ? node.id : (node.name || node.id)
    
    // If the mode changed (Permanent vs Hover) or the text needs updating, re-bind
    if (isCurrentlyPermanent !== props.showNodeIds || currentTooltip?.getContent() !== desiredText) {
      marker.unbindTooltip()
      marker.bindTooltip(desiredText, { 
        permanent: props.showNodeIds, 
        direction: 'top', 
        className: props.showNodeIds ? 'node-id-tooltip' : '' 
      })
    }
  })
}

const updateStaticLayers = () => {
  if (!mapInstance) return

  const nodeLookup = {}
  props.allNodes.forEach(n => { nodeLookup[n.id] = n })

  staticEdgeLinesLayer.clearLayers()
  if (props.showGraphEdges && props.staticEdges && props.staticEdges.length > 0) {
    props.staticEdges.forEach(edge => {
      const source = nodeLookup[edge.source]
      const target = nodeLookup[edge.target]
      if (source && target) {
        const isNegative = edge.weight < 0
        const line = L.polyline([[source.latitude, source.longitude], [target.latitude, target.longitude]], {
          color: isNegative ? '#ef4444' : '#64748b', 
          weight: isNegative ? 5 : 3,
          dashArray: isNegative ? '5, 10' : null,
          opacity: 0.6,
          bubblingMouseEvents: true // Let clicks pass through to map
        })
        line.bindTooltip(`${isNegative ? 'Priority' : 'Edge'}: ${edge.weight?.toFixed(2)}`, { sticky: true })
        staticEdgeLinesLayer.addLayer(line)
      }
    })
  }

  updateNodeStyles()
}

const updateAlgoLayers = () => {
  if (!mapInstance) return

  const nodeLookup = {}
  props.allNodes.forEach(n => { nodeLookup[n.id] = n })

  if (relaxedEdgeLine) mapInstance.removeLayer(relaxedEdgeLine)
  if (props.currentRelaxedEdge) {
    const source = nodeLookup[props.currentRelaxedEdge.sourceId]
    const target = nodeLookup[props.currentRelaxedEdge.targetId]
    if (source && target) {
      relaxedEdgeLine = L.polyline([[source.latitude, source.longitude], [target.latitude, target.longitude]], {
        color: '#fbbf24',
        weight: 8,
        opacity: 1
      }).addTo(mapInstance)
    }
  }

  algoEdgeLinesLayer.clearLayers()
  if (props.allEdges && props.allEdges.length > 0) {
    props.allEdges.forEach(edge => {
      const source = nodeLookup[edge.source] || nodeLookup[edge.sourceId]
      const target = nodeLookup[edge.target] || nodeLookup[edge.targetId]
      if (source && target) {
        const isNegative = edge.weight < 0
        const line = L.polyline([[source.latitude, source.longitude], [target.latitude, target.longitude]], {
          color: isNegative ? '#ef4444' : '#64748b', 
          weight: isNegative ? 6 : 4,
          dashArray: isNegative ? '5, 10' : null,
          opacity: 0.9,
          interactive: false // Prevent step edges from being clickable
        })
        algoEdgeLinesLayer.addLayer(line)
      }
    })
  }
  
  updateNodeStyles()
  
  // Ensure the final calculated path stays in front of the algorithm visualization edges
  if (polylineLayer) {
    polylineLayer.bringToFront()
  }
}

const updatePathLayer = (shouldFit = false) => {
  if (!mapInstance) return

  if (polylineLayer) mapInstance.removeLayer(polylineLayer)
  markersLayer.clearLayers()

  if (props.pathCoordinates && props.pathCoordinates.length >= 2) {
    const pathCoordsList = props.pathCoordinates.map((node) => [
      node.lat || node.latitude, 
      node.lng || node.longitude
    ])
    
    polylineLayer = L.polyline(pathCoordsList, {
      color: '#22c55e',
      opacity: 0.9,
      weight: 8,
      interactive: false // Prevent final path from being clickable
    }).addTo(mapInstance)

    props.pathCoordinates.forEach((node, index) => {
      const isStart = index === 0
      const isEnd = index === props.pathCoordinates.length - 1
      if (!isStart && !isEnd) return 

      const lat = node.lat || node.latitude
      const lng = node.lng || node.longitude
      
      const marker = L.circleMarker([lat, lng], {
        radius: 10,
        fillColor: isStart ? '#22c55e' : '#15803d',
        color: '#ffffff',
        weight: 3,
        opacity: 1,
        fillOpacity: 1,
        pane: 'markerPane'
      })
      markersLayer.addLayer(marker)
    })
    
    if (shouldFit) {
      mapInstance.fitBounds(polylineLayer.getBounds(), { padding: [50, 50] })
    }
  }
}

watch(() => [props.allNodes, props.showMapNodes], () => {
  syncMarkers()
  updateStaticLayers()
}, { deep: true })

watch(() => [props.staticEdges, props.showGraphEdges, props.showNodeIds], () => {
  updateStaticLayers()
}, { deep: true })

watch(() => [props.allEdges, props.currentTraversedNode, props.currentRelaxedEdge, props.visitedNodes], () => {
  updateAlgoLayers()
}, { deep: true })

watch(() => props.pathCoordinates, (newVal, oldVal) => {
  const changed = JSON.stringify(newVal) !== JSON.stringify(oldVal)
  updatePathLayer(changed)
}, { deep: true })

onUnmounted(() => {
  if (resizeObserver) resizeObserver.disconnect()
  if (mapInstance) mapInstance.remove()
})
</script>
