import { useState, useCallback } from 'react';

import ReactFlow, {
    addEdge,
    FitViewOptions,
    applyNodeChanges,
    applyEdgeChanges,
    Node,
    Edge,
    NodeChange,
    EdgeChange,
    Connection,
    DefaultEdgeOptions,
    NodeTypes,
    OnNodesChange,
    OnEdgesChange,
    OnConnect,
    MarkerType,
} from 'reactflow';

import { CourseNode } from './CourseNode';

const initialNodes: Node[] = [
    { id: 'ds', type: 'custom', data: { 
        title: "Data Structures",
        offeringName: "EN.601.226",
        description: "filler",
        prerequisiteString: "EN.500.132 OR (EN.500.112 or EN.601.220) or AP Computer Science or equivalent.",
        prerequisiteList: [],
        level: 1,
        connectingTo: []
    }, position: { x: 300, y: 0 } },

    { id: 'gateway', type: 'custom', data: { 
        title: "Gateway Computing: JAVA",
        offeringName: "EN.500.112",
        description: "filler",
        prerequisiteString: "",
        prerequisiteList: [],
        level: 0,
        connectingTo: []
    }, position: { x: 0, y: 0 } },

];

const initialEdges: Edge[] = [
    { 
        id: 'e2-1', 
        source: 'gateway', 
        target: 'ds', 
        markerEnd: {
            type: MarkerType.Arrow,
        }, 
    }
];

const fitViewOptions: FitViewOptions = {
    padding: 0.2,
};    
   
const defaultEdgeOptions: DefaultEdgeOptions = {
    animated: true,
    style: { stroke: '#555', strokeWidth: 4, transition: 'stroke 0.3s ease-out', }
};

const nodeTypes: NodeTypes = {
    custom: CourseNode,
};


const ExampleGraph = () => {
    const [nodes, setNodes] = useState<Node[]>(initialNodes);
    const [edges, setEdges] = useState<Edge[]>(initialEdges);

    const onNodesChange: OnNodesChange = useCallback(
        (changes) => setNodes((nds) => applyNodeChanges(changes, nds)),
        [setNodes]
    );

    const onEdgesChange: OnEdgesChange = useCallback(
        (changes) => setEdges((eds) => applyEdgeChanges(changes, eds)),
        [setEdges],
    );

    const onConnect: OnConnect = useCallback(
        (connection) => setEdges((eds) => addEdge(connection, eds)),
        [setEdges],
    );
    
    console.log(`Number of nodes: ${nodes.length}`);
    console.log(`Number of edges: ${edges.length}`);


    return (
        <div style={{width: '100vw', height: '100vh'}}>
            <ReactFlow
            nodes={nodes}
            edges={edges}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            onConnect={onConnect}
            fitView
            fitViewOptions={fitViewOptions}
            defaultEdgeOptions={defaultEdgeOptions}
            nodeTypes={nodeTypes}
        />
        </div>
        
    );
}

export default ExampleGraph;