import React, { useCallback } from 'react';
import ReactFlow, { useNodesState, useEdgesState, addEdge } from 'reactflow';
import 'reactflow/dist/style.css';

const getNodes = () => {
    // JSON object-like structure
    /*
        id
        position: {<x coord>, <y coord>}
        data: {label: 'one', <more data>}
    */
    return (
        [
            { id: '1', position: { x: 0, y: 0 }, data: { label: 'one' } },
            { id: '2', position: { x: 0, y: 100 }, data: { label: 'two' } },
            { id: '3', position: { x: 0, y: 200 }, data: { label: 'three' } },
        ]
    );
}

const getEdges = () => {
    /*
        id
        source: id of source node
        target: id of target node
    */
    return (
        [
            { id: 'e1-2', source: '1', target: '2' },
            { id: 'e2-3', source: '2', target: '3'}
        ]
    );
}


const Graph = () => {
    const initialNodes = getNodes();
    const initialEdges = getEdges();

    const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
    const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

    const onConnect = useCallback(
        (params) => setEdges((eds) => addEdge(params, eds)),
        [setEdges],
    );

    return (
        <div style={{ width: '100vw', height: '100vh' }}>
          <ReactFlow
            nodes={nodes}
            edges={edges}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            onConnect={onConnect}
          />
        </div>
      );

} 

export default Graph;