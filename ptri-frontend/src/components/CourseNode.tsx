import { useCallback } from 'react';
import { Node, NodeProps, Handle, Position } from 'reactflow';
import '../stylesheets/CourseNode.css';

type CourseProps = {
    title: string;
    offeringName: string;
    description: string;
    prerequisiteString: string;
    prerequisiteList: Object;
    level: number;
    connectingTo: Object;
};

// ! what does this mean
// const handleStyle = { left: 10 };

export type CourseNodeType = Node<CourseProps>;

const CourseNode = ( {data, isConnectable} : NodeProps<CourseProps> ) => {

    // const onChange = useCallback((evt: any) => {
    //     console.log(evt.target.value);
    //   }, []);
    return (
        <div className="course-node">
            <Handle type="target" position={Position.Left} isConnectable={isConnectable} />
            <div>
                <h4 className="course-node-title-text">{data.title}</h4>
                <h5 className="course-node-offering-name-text">{data.offeringName}</h5>
            </div>
            <Handle type="source" position={Position.Right} isConnectable={isConnectable} />
        </div>
    );
}

export { CourseNode };
