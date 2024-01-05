import React, { useCallback } from 'react';
import ReactFlow, { useNodesState, useEdgesState, addEdge } from 'reactflow';
import 'reactflow/dist/style.css';
import axios from 'axios';

const BASE_URL = "localhost:8080/";

type CourseNode = {
    id: string;
    position: {
        x: number;
        y: number;
    };
    data: {
        label: string;
        offeringName: string;
    };
}

type CourseEdge = {
    id: string;
    source: string;
    target: string;
}

const generateNodesAndEdges = () => {
    // JSON object-like structure
    /*
        id
        position: {<x coord>, <y coord>}
        data: {label: 'one', <more data>}
    */

    // ! Just for testing purposes
    const initialNodes: CourseNode[] = [
        { id: 'EN.601.226', position: { x: 200, y: 200 }, data: { 
            label: 'Data Structures',
            offeringName: 'EN.601.226'
        }},
        { id: 'EN.601.220', position: { x: 0, y: 0 }, data: { 
            label: 'Intermediate Programming',
            offeringName: 'EN.601.220'
        }},
        { id: 'EN.500.132', position: { x: 0, y: 200 }, data: { 
            label: 'Bootcamp: Java',
            offeringName: 'EN.500.132'
        }},
        { id: 'EN.500.112', position: { x: 0, y: 400 }, data: { 
            label: 'Gateway Computing: JAVA',
            offeringName: 'EN.500.112'
        }},
    
    ];

    // ! Just for testing purposes
    const initialEdges: CourseEdge[] = [
        { id: 'e1', source: 'EN.601.220', target: 'EN.601.226' },
        { id: 'e2', source: 'EN.500.132', target: 'EN.601.226'},
        { id: 'e3', source: 'EN.500.112', target: 'EN.601.226'}

    ];
   
    
    axios.get(BASE_URL + "courses")
        .then(function (response) {
            // Create all nodes


            // Create all edges
        })
        .catch(function (error) {

        }).finally(function () {

        });
    
    return (
        [initialNodes, initialEdges]
    );
}



const Graph = () => {
    const nodesAndEdges = generateNodesAndEdges();

    const initialNodes = nodesAndEdges[0];
    const initialEdges = nodesAndEdges[1];

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